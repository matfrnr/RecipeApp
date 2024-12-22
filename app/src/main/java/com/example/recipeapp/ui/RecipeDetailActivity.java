package com.example.recipeapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Comment;
import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.RecipeIngredient;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;
import java.util.Objects;

public class RecipeDetailActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private Recipe recipe;
    private TextView titleText;
    private TextView descriptionText;
    private TextView cookingTimeText;
    private TextView difficultyText;
    private TextView servingsText;
    private TextView stepsText;
    private TextView authorText;
    private ImageView recipeImage;
    private Menu menu;
    private RatingBar ratingBar;
    private TextView ratingText;
    private View ratingContainer;
    private EditText portionEditText;
    private Button calculateButton;
    private RecyclerView ingredientsRecyclerView;
    private IngredientsAdapter ingredientsAdapter;
    private int originalServings;
    private Button openCalculatorButton;
    private BottomSheetDialog calculatorDialog;
    private View calculatorView;
    private RecyclerView commentsRecyclerView;
    private CommentAdapter commentAdapter;
    private EditText commentInput;
    private Button sendCommentButton;
    private BottomSheetDialog commentBottomSheet;
    private Button commentsButton;
    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        @androidx.annotation.RequiresApi(api = android.os.Build.VERSION_CODES.TIRAMISU)
        public void onReceive(Context context, Intent intent) {
            if ("com.example.recipeapp.RECIPE_UPDATED".equals(intent.getAction())) {
                reloadRecipe();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dbHelper = new DatabaseHelper(this);
        initializeViews();

        long recipeId = getIntent().getLongExtra("recipe_id", -1);
        if (recipeId != -1) {
            recipe = dbHelper.getRecipe((int) recipeId);
            if (recipe != null) {
                displayRecipe();
                setupCalculatorDialog(); // Ajoutez ici l'appel
            }
        }


        if (recipe.isCommunity()) {
            displayRecipe();
            setupCommentsButton();
        }


        ratingBar = findViewById(R.id.ratingBar);
        ratingText = findViewById(R.id.ratingText);
        ratingContainer = findViewById(R.id.ratingContainer);
        setupRating();
    }


    private void setupCommentsButton() {
        commentsButton = findViewById(R.id.commentsButton); // Assurez-vous d'ajouter ce bouton dans votre layout
        commentsButton.setOnClickListener(v -> showCommentsDialog());
    }

    private void showCommentsDialog() {
        // Créer le bottom sheet s'il n'existe pas
        if (commentBottomSheet == null) {
            commentBottomSheet = new BottomSheetDialog(this);
            View bottomSheetView = getLayoutInflater().inflate(R.layout.layout_comment_bottom_sheet, null);
            commentBottomSheet.setContentView(bottomSheetView);

            // Initialiser les vues
            RecyclerView recyclerView = bottomSheetView.findViewById(R.id.commentsRecyclerView);
            EditText commentInput = bottomSheetView.findViewById(R.id.commentInput);
            ImageButton sendButton = bottomSheetView.findViewById(R.id.sendCommentButton);

            // Configurer le RecyclerView
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            List<Comment> comments = dbHelper.getRecipeComments(recipe.getId());
            CommentAdapter adapter = new CommentAdapter(this, comments, getCurrentUserId());
            recyclerView.setAdapter(adapter);

            // Configurer le bouton d'envoi
            sendButton.setOnClickListener(v -> {
                String content = commentInput.getText().toString().trim();
                if (!content.isEmpty()) {
                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    int userId = prefs.getInt("userId", -1);
                    String username = prefs.getString("username", "");

                    if (userId != -1) {
                        // Ajouter le commentaire
                        dbHelper.addComment(userId, recipe.getId(), content, username);

                        // Créer et ajouter le nouveau commentaire à l'adapter
                        Comment newComment = new Comment(
                                0,
                                userId,
                                recipe.getId(),
                                content,
                                System.currentTimeMillis(),
                                username
                        );
                        adapter.addComment(newComment);

                        // Effacer l'input et scroll vers le haut
                        commentInput.setText("");
                        recyclerView.smoothScrollToPosition(0);
                    } else {
                        Toast.makeText(this, "Connectez-vous pour commenter", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                    }
                }
            });
        }

        // Afficher le bottom sheet
        commentBottomSheet.show();
    }


    private int getCurrentUserId() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getInt("userId", -1);
    }

    private void setupComments() {
        View commentSection = findViewById(R.id.commentSection);
        commentSection.setVisibility(View.VISIBLE);

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        sendCommentButton = findViewById(R.id.sendCommentButton);

        // Configuration du RecyclerView
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<Comment> comments = dbHelper.getRecipeComments(recipe.getId());
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int currentUserId = prefs.getInt("userId", -1);
        commentAdapter = new CommentAdapter(this, comments, currentUserId);
        commentsRecyclerView.setAdapter(commentAdapter);

        // Configuration du bouton d'envoi
        sendCommentButton.setOnClickListener(v -> {
            String content = commentInput.getText().toString().trim();
            if (!content.isEmpty()) {
                int userId = prefs.getInt("userId", -1);
                String username = prefs.getString("username", "");

                if (userId != -1) {
                    dbHelper.addComment(userId, recipe.getId(), content, username);

                    // Créer et ajouter le nouveau commentaire
                    Comment newComment = new Comment(
                            0, // l'ID sera généré par la base de données
                            userId,
                            recipe.getId(),
                            content,
                            System.currentTimeMillis(),
                            username
                    );
                    commentAdapter.addComment(newComment);
                    commentsRecyclerView.scrollToPosition(0);

                    // Effacer le champ de saisie
                    commentInput.setText("");
                } else {
                    Toast.makeText(this, "Vous devez être connecté pour commenter",
                            Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, LoginActivity.class));
                }
            }
        });
    }

    private void setupCalculatorDialog() {
        openCalculatorButton = findViewById(R.id.openCalculatorButton);
        if (openCalculatorButton == null) {
            return;
        }

        calculatorDialog = new BottomSheetDialog(this);
        calculatorView = getLayoutInflater().inflate(R.layout.layout_portion_calculator, null);
        calculatorDialog.setContentView(calculatorView);

        portionEditText = calculatorView.findViewById(R.id.portionEditText);
        ingredientsRecyclerView = calculatorView.findViewById(R.id.ingredientsRecyclerView);
        Button calculateButton = calculatorView.findViewById(R.id.calculateButton);

        List<RecipeIngredient> ingredients = dbHelper.getRecipeIngredients(recipe.getId());

        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        ingredientsAdapter = new IngredientsAdapter(ingredients);
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);

        originalServings = recipe.getServings();
        portionEditText.setText(String.valueOf(originalServings));

        calculateButton.setOnClickListener(v -> {
            try {
                int newServings = Integer.parseInt(portionEditText.getText().toString());
                if (newServings > 0 && newServings <= 10) {
                    double multiplier = (double) newServings / originalServings;
                    ingredientsAdapter.updatePortions(multiplier);
                } else {
                    Toast.makeText(this, "Nombre invalide (max 10)", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Nombre invalide (max 10)", Toast.LENGTH_SHORT).show();
            }
        });

        openCalculatorButton.setOnClickListener(v -> {
            calculatorDialog.show();
        });

        Button addToShoppingListButton = calculatorView.findViewById(R.id.addToShoppingListButton);
        addToShoppingListButton.setOnClickListener(v -> {
            // Récupérer les ingrédients avec les quantités calculées
            List<RecipeIngredient> currentIngredients = ingredientsAdapter.getCurrentIngredients();
            dbHelper.addToShoppingList(recipe.getName(), currentIngredients);
            Toast.makeText(this, "Ajouté à la liste de courses", Toast.LENGTH_SHORT).show();
            calculatorDialog.dismiss();
        });
    }

    private void setupPortionCalculator() {
        portionEditText = findViewById(R.id.portionEditText);
        calculateButton = findViewById(R.id.calculateButton);
        ingredientsRecyclerView = findViewById(R.id.ingredientsRecyclerView);

        // Configurer le RecyclerView
        ingredientsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        List<RecipeIngredient> ingredients = dbHelper.getRecipeIngredients(recipe.getId());
        ingredientsAdapter = new IngredientsAdapter(ingredients);
        ingredientsRecyclerView.setAdapter(ingredientsAdapter);

        // Initialiser avec le nombre de portions original
        originalServings = recipe.getServings();
        portionEditText.setText(String.valueOf(originalServings));

        calculateButton.setOnClickListener(v -> {
            try {
                int newServings = Integer.parseInt(portionEditText.getText().toString());
                if (newServings > 0) {
                    double multiplier = (double) newServings / originalServings;
                    ingredientsAdapter.updatePortions(multiplier);
                } else {
                    Toast.makeText(this, "Veuillez entrer un nombre valide", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Veuillez entrer un nombre valide", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void setupRating() {
        if (recipe != null) {  // Supprimer la vérification de isCommunity
            ratingContainer.setVisibility(View.VISIBLE);
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("userId", -1);

            // Charger la note moyenne et la note de l'utilisateur
            float averageRating = dbHelper.getAverageRating((int) recipe.getId());
            int ratingCount = dbHelper.getRatingCount((int) recipe.getId());

            if (userId != -1) {
                float userRating = dbHelper.getUserRating(userId, (int) recipe.getId());
                ratingBar.setRating(userRating);
                ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                    if (fromUser) {
                        dbHelper.rateRecipe(userId, (int) recipe.getId(), rating);
                        updateRatingDisplay();
                        Intent intent = new Intent("com.example.recipeapp.RECIPE_UPDATED")
                                .setPackage(getPackageName());
                        sendBroadcast(intent);
                    }
                });
            } else {
                ratingBar.setIsIndicator(true); // Lecture seule si non connecté
                Toast.makeText(this, "Connectez-vous pour noter cette recette", Toast.LENGTH_SHORT).show();
            }

            updateRatingDisplay();
        } else {
            ratingContainer.setVisibility(View.GONE);
        }
    }

    private void updateRatingDisplay() {
        float averageRating = dbHelper.getAverageRating((int) recipe.getId());
        int ratingCount = dbHelper.getRatingCount((int) recipe.getId());

        String ratingFormat = String.format("%.1f/5 (%d avis)", averageRating, ratingCount);
        ratingText.setText(ratingFormat);
    }


    private void initializeViews() {
        recipeImage = findViewById(R.id.recipeImage);
        titleText = findViewById(R.id.recipeTitleText);
        descriptionText = findViewById(R.id.recipeDescriptionText);
        cookingTimeText = findViewById(R.id.cookingTimeText);
        difficultyText = findViewById(R.id.difficultyText);
        servingsText = findViewById(R.id.servingsText);
        stepsText = findViewById(R.id.stepsText);
        authorText = findViewById(R.id.authorText);
    }

    private void displayRecipe() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(recipe.getName());
        }

        titleText.setText(recipe.getName());
        descriptionText.setText(recipe.getDescription());
        cookingTimeText.setText(String.format("Temps de préparation : %d minutes", recipe.getCookingTime()));
        difficultyText.setText(String.format("Difficulté : %s", recipe.getDifficulty()));
        servingsText.setText(String.format("Pour %d personnes", recipe.getServings()));
        stepsText.setText(recipe.getSteps());

        if (!Objects.equals(recipe.getAuthor(), "Admin")) {
            authorText.setVisibility(View.VISIBLE);
            authorText.setText(String.format("Par %s", recipe.getAuthor()));
        } else {
            authorText.setVisibility(View.GONE);
        }

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            if (recipe.getImageUrl().startsWith("@drawable/")) {
                int resourceId = getResources().getIdentifier(
                        recipe.getImageUrl().replace("@drawable/", ""),
                        "drawable",
                        getPackageName()
                );
                if (resourceId != 0) {
                    recipeImage.setImageResource(resourceId);
                } else {
                    recipeImage.setImageResource(R.drawable.default_recipe_image);
                }
            } else {
                try {
                    Uri imageUri = Uri.parse(recipe.getImageUrl());
                    recipeImage.setImageURI(imageUri);
                } catch (Exception e) {
                    recipeImage.setImageResource(R.drawable.default_recipe_image);
                }
            }
        } else {
            recipeImage.setImageResource(R.drawable.default_recipe_image);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        MenuItem editItem = menu.findItem(R.id.action_edit);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);

        boolean isAuthor = (recipe != null && userId == recipe.getUserId());
        editItem.setVisible(isAuthor);
        deleteItem.setVisible(isAuthor);

        updateFavoriteIcon();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();  // Au lieu de onBackPressed()
            return true;
        } else if (itemId == R.id.action_favorite) {
            toggleFavorite();
            return true;
        } else if (itemId == R.id.action_edit) {
            Intent intent = new Intent(this, EditRecipeActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_delete) {
            showDeleteConfirmationDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void toggleFavorite() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1 && recipe != null) {
            dbHelper.toggleFavorite(userId, (int) recipe.getId());
            updateFavoriteIcon();
            // Correction du broadcast
            Intent intent = new Intent("com.example.recipeapp.RECIPE_UPDATED")
                    .setPackage(getPackageName());
            sendBroadcast(intent);
        } else {
            Toast.makeText(this, "Veuillez vous connecter pour ajouter aux favoris", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateFavoriteIcon() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        MenuItem favoriteItem = menu.findItem(R.id.action_favorite);
        if (favoriteItem != null && userId != -1 && recipe != null) {
            boolean isFavorite = dbHelper.isFavorite(userId, (int) recipe.getId());
            favoriteItem.setIcon(isFavorite ?
                    R.drawable.ic_favorite_filled :
                    R.drawable.ic_favorite);
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Supprimer la recette")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette recette ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    dbHelper.deleteRecipe(recipe.getId());
                    // Correction du broadcast
                    Intent intent = new Intent("com.example.recipeapp.RECIPE_UPDATED")
                            .setPackage(getPackageName());
                    sendBroadcast(intent);
                    finish();
                })
                .setNegativeButton("Non", null)
                .show();
    }

    private void reloadRecipe() {
        if (recipe != null) {
            recipe = dbHelper.getRecipe((int) recipe.getId());
            if (recipe != null) {
                displayRecipe();
                invalidateOptionsMenu();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.recipeapp.RECIPE_UPDATED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter); // Ajout de l'enregistrement pour les versions antérieures
        }
        reloadRecipe();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
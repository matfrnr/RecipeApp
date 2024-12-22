package com.example.recipeapp.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {
    private List<Recipe> recipes;
    private final Context context;
    private final boolean isCommunityList;
    private TextView dietTypeText;
    private TextView allergensText;

    public RecipeAdapter(Context context, List<Recipe> recipes, boolean isCommunityList) {
        this.context = context;
        this.recipes = recipes;
        this.isCommunityList = isCommunityList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.bind(recipe);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.getId());
            context.startActivity(intent);
        });

        // Gérer les options selon le type de liste
        if (isCommunityList) {
            SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int currentUserId = prefs.getInt("userId", -1);

            if (currentUserId == recipe.getUserId()) {
                holder.itemView.setOnLongClickListener(v -> {
                    showRecipeOptions(v, recipe, position);
                    return true;
                });
            }
        }
    }

    private void showRecipeOptions(View view, Recipe recipe, int position) {
        PopupMenu popup = new PopupMenu(context, view);
        popup.getMenuInflater().inflate(R.menu.menu_recipe_options, popup.getMenu());

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit) {
                Intent intent = new Intent(context, EditRecipeActivity.class);
                intent.putExtra("recipe_id", recipe.getId());
                context.startActivity(intent);
                return true;
            } else if (item.getItemId() == R.id.action_delete) {
                showDeleteConfirmationDialog(recipe, position);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showDeleteConfirmationDialog(Recipe recipe, int position) {
        new AlertDialog.Builder(context)
                .setTitle("Supprimer la recette")
                .setMessage("Êtes-vous sûr de vouloir supprimer cette recette ?")
                .setPositiveButton("Oui", (dialog, which) -> {
                    DatabaseHelper dbHelper = new DatabaseHelper(context);
                    dbHelper.deleteRecipe(recipe.getId());
                    recipes.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, recipes.size());
                    dbHelper.close();
                    context.sendBroadcast(new Intent("RECIPE_UPDATED"));
                })
                .setNegativeButton("Non", null)
                .show();
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public void updateRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText;
        private final TextView descriptionText;
        private final TextView cookingTimeText;
        private final TextView difficultyText;
        private final TextView authorText;
        private final ImageView recipeImage;
        private final ImageButton favoriteButton;
        private final TextView ratingText; // Nouveau TextView pour les notes
        private final TextView dietTypeText;
        private final TextView allergensText;

        ViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.recipeTitleText);
            descriptionText = view.findViewById(R.id.recipeDescriptionText);
            cookingTimeText = view.findViewById(R.id.cookingTimeText);
            difficultyText = view.findViewById(R.id.difficultyText);
            authorText = view.findViewById(R.id.authorText);
            recipeImage = view.findViewById(R.id.recipeImage);
            favoriteButton = view.findViewById(R.id.favoriteButton);
            ratingText = view.findViewById(R.id.ratingText); // Initialiser le TextView des notes
            dietTypeText = view.findViewById(R.id.dietTypeText);     // Ajouter ces
            allergensText = view.findViewById(R.id.allergensText);   // deux lignes
        }

        // Nouvelle méthode pour mettre à jour l'état du bouton favori
        private void updateFavoriteButtonState(boolean isFavorite) {
            favoriteButton.setImageResource(isFavorite ?
                    R.drawable.ic_favorite_filled :
                    R.drawable.ic_favorite);
        }

        void bind(Recipe recipe) {
            titleText.setText(recipe.getName());
            descriptionText.setText(recipe.getDescription());
            cookingTimeText.setText(String.format("%d min", recipe.getCookingTime()));
            difficultyText.setText(recipe.getDifficulty());

            DatabaseHelper dbHelper = new DatabaseHelper(context);
            try {
                // Gestion des notes pour les recettes communautaires
                if (recipe.isCommunity()) {
                    float avgRating = dbHelper.getAverageRating((int) recipe.getId());
                    int ratingCount = dbHelper.getRatingCount((int) recipe.getId());

                    if (ratingCount > 0) {
                        ratingText.setVisibility(View.VISIBLE);
                        ratingText.setText(String.format("%.1f/5 (%d avis)", avgRating, ratingCount));
                    } else {
                        ratingText.setVisibility(View.VISIBLE);
                        ratingText.setText("Pas encore d'avis");
                    }
                } else {
                    ratingText.setVisibility(View.GONE);
                }

                // Afficher le régime si présent
                if (recipe.getDietType() != null && !recipe.getDietType().equals("none")) {
                    dietTypeText.setVisibility(View.VISIBLE);

                    // Définir le texte et appliquer une couleur spécifique au type de régime
                    switch (recipe.getDietType()) {
                        case "vegetarian":
                            dietTypeText.setText("Végétarien");
                            dietTypeText.setTextColor(ContextCompat.getColor(context, R.color.vegetarian_color));
                            break;
                        case "vegan":
                            dietTypeText.setText("Végan");
                            dietTypeText.setTextColor(ContextCompat.getColor(context, R.color.vegetarian_color));
                            break;
                        case "gluten_free":
                            dietTypeText.setText("Sans gluten");
                            dietTypeText.setTextColor(ContextCompat.getColor(context, R.color.vegetarian_color));
                            break;
                        default:
                            dietTypeText.setVisibility(View.GONE); // Ne devrait pas se produire avec "none" déjà vérifié
                            break;
                    }
                } else {
                    dietTypeText.setVisibility(View.GONE);
                }


                if (!recipe.getAllergens().isEmpty()) {
                    allergensText.setVisibility(View.VISIBLE);
                } else {
                    allergensText.setVisibility(View.GONE);
                }

                // Gestion de l'auteur
                if (isCommunityList) {
                    authorText.setVisibility(View.VISIBLE);
                    authorText.setText(String.format("Par %s", recipe.getAuthor()));
                } else {
                    authorText.setVisibility(View.GONE);
                }

                // Gestion des favoris
                SharedPreferences prefs = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                int userId = prefs.getInt("userId", -1);

                if (userId != -1) {
                    favoriteButton.setVisibility(View.VISIBLE);
                    // Configuration initiale
                    updateFavoriteButtonState(dbHelper.isFavorite(userId, (int) recipe.getId()));

                    favoriteButton.setOnClickListener(v -> {
                        DatabaseHelper clickDbHelper = new DatabaseHelper(context);
                        try {
                            // Toggle le favori dans la base de données
                            clickDbHelper.toggleFavorite(userId, (int) recipe.getId());
                            // Mettre à jour l'état visuel en vérifiant la base de données
                            updateFavoriteButtonState(clickDbHelper.isFavorite(userId, (int) recipe.getId()));
                            // Envoyer le broadcast
                            Intent intent = new Intent("com.example.recipeapp.RECIPE_UPDATED")
                                    .setPackage(context.getPackageName());
                            context.sendBroadcast(intent);
                        } finally {
                            clickDbHelper.close();
                        }
                    });
                } else {
                    favoriteButton.setVisibility(View.GONE);
                }

                // Gestion de l'image
                if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
                    if (recipe.getImageUrl().startsWith("@drawable/")) {
                        int resourceId = context.getResources().getIdentifier(
                                recipe.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                context.getPackageName()
                        );
                        recipeImage.setImageResource(resourceId != 0 ? resourceId : R.drawable.default_recipe_image);
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
            } finally {
                dbHelper.close();
            }
        }
    }

}
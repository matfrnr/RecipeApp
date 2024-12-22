package com.example.recipeapp.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;
import java.util.List;
import java.util.Random;

public class DailyRecipeFragment extends Fragment {
    private static final String PREFS_NAME = "RecipePrefs";
    private static final String LAST_UPDATE_DATE = "lastUpdateDate";
    private static final String CURRENT_RECIPE_NAME = "currentRecipeName";

    private DatabaseHelper dbHelper;
    private ImageView recipeImageView;
    private TextView titleText, descriptionText, stepsText;
    private TextView cookingTimeText, difficultyText, servingsText;
    private Button randomRecipeButton;
    private RatingBar ratingBar;
    private TextView ratingText;
    private View ratingContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_daily_recipe, container, false);
        dbHelper = new DatabaseHelper(getContext());

        initViews(view);

        // Initialisation des vues pour la notation
        ratingBar = view.findViewById(R.id.ratingBar);
        ratingText = view.findViewById(R.id.ratingText);
        ratingContainer = view.findViewById(R.id.ratingContainer);

        // Configurer le bouton pour nouvelle recette
        randomRecipeButton.setOnClickListener(v -> forceNewRecipe());

        // Vérifier et charger la recette
        checkAndUpdateDailyRecipe();

        return view;
    }
    private void initViews(View view) {
        recipeImageView = view.findViewById(R.id.recipeImage);
        titleText = view.findViewById(R.id.recipeTitleText);
        descriptionText = view.findViewById(R.id.recipeDescriptionText);
        stepsText = view.findViewById(R.id.stepsText);
        cookingTimeText = view.findViewById(R.id.cookingTimeText);
        difficultyText = view.findViewById(R.id.difficultyText);
        servingsText = view.findViewById(R.id.servingsText);
        randomRecipeButton = view.findViewById(R.id.randomRecipeButton);
    }

    private void checkAndUpdateDailyRecipe() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        long lastUpdate = prefs.getLong(LAST_UPDATE_DATE, 0);
        String currentRecipeName = prefs.getString(CURRENT_RECIPE_NAME, "");
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastUpdate > 24 * 60 * 60 * 1000 || currentRecipeName.isEmpty()) {
            forceNewRecipe();
        } else {
            List<Recipe> recipes = dbHelper.getAllRecipes();
            for (Recipe recipe : recipes) {
                if (recipe.getName().equals(currentRecipeName)) {
                    displayRecipe(recipe);
                    break;
                }
            }
        }
    }

    private void forceNewRecipe() {
        Recipe recipe = getRandomRecipe();
        if (recipe != null) {
            SharedPreferences.Editor editor = requireContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit();
            editor.putLong(LAST_UPDATE_DATE, System.currentTimeMillis());
            editor.putString(CURRENT_RECIPE_NAME, recipe.getName());
            editor.apply();

            displayRecipe(recipe);
        }
    }

    private Recipe getRandomRecipe() {
        List<Recipe> recipes = dbHelper.getAllRecipes(); // Ceci ne retournera que les recettes non communautaires
        if (!recipes.isEmpty()) {
            int randomIndex = new Random().nextInt(recipes.size());
            return recipes.get(randomIndex);
        }
        return null;
    }

    private void displayRecipe(Recipe recipe) {
        titleText.setText(recipe.getName());
        descriptionText.setText(recipe.getDescription());
        stepsText.setText(recipe.getSteps());
        cookingTimeText.setText(String.format("Temps de cuisson : %d minutes", recipe.getCookingTime()));
        difficultyText.setText(String.format("Difficulté : %s", recipe.getDifficulty()));
        servingsText.setText(String.format("Pour %d personnes", recipe.getServings()));
        setupRating(recipe);

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            int resourceId = getResources().getIdentifier(
                    recipe.getImageUrl().replace("@drawable/", ""),
                    "drawable",
                    requireContext().getPackageName()
            );
            if (resourceId != 0) {
                recipeImageView.setImageResource(resourceId);
            } else {
                recipeImageView.setImageResource(R.drawable.default_recipe_image);
            }
        } else {
            recipeImageView.setImageResource(R.drawable.default_recipe_image);
        }
    }

    private void setupRating(Recipe recipe) {
        if (recipe != null) {
            ratingContainer.setVisibility(View.VISIBLE);
            SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            int userId = prefs.getInt("userId", -1);

            float averageRating = dbHelper.getAverageRating((int) recipe.getId());
            int ratingCount = dbHelper.getRatingCount((int) recipe.getId());

            if (userId != -1) {
                float userRating = dbHelper.getUserRating(userId, (int) recipe.getId());
                ratingBar.setRating(userRating);ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                    if (fromUser) {
                        dbHelper.rateRecipe(userId, (int) recipe.getId(), rating);
                        updateRatingDisplay(recipe.getId());
                        // Notifier les autres vues
                        Intent intent = new Intent("com.example.recipeapp.RECIPE_UPDATED")
                                .setPackage(requireContext().getPackageName());
                        requireContext().sendBroadcast(intent);
                    }
                });
            } else {
                ratingBar.setIsIndicator(true);
                Toast.makeText(getContext(), "Connectez-vous pour noter cette recette", Toast.LENGTH_SHORT).show();
            }  updateRatingDisplay(recipe.getId());
        } else {
            ratingContainer.setVisibility(View.GONE);
        }
    }

    private void updateRatingDisplay(long recipeId) {
        float averageRating = dbHelper.getAverageRating((int) recipeId);
        int ratingCount = dbHelper.getRatingCount((int) recipeId);

        String ratingFormat = String.format("%.1f/5 (%d avis)", averageRating, ratingCount);
        ratingText.setText(ratingFormat);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
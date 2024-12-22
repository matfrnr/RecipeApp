package com.example.recipeapp.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.models.RecipeIngredient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsAdapter.ViewHolder> {
    private List<RecipeIngredient> ingredients;
    private double portionMultiplier = 1.0;
    private List<RecipeIngredient> visibleIngredients; // Pour gérer les ingrédients visibles

    public IngredientsAdapter(List<RecipeIngredient> ingredients) {
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>();
        this.visibleIngredients = new ArrayList<>(this.ingredients);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new ViewHolder(view);
    }

    public List<RecipeIngredient> getCurrentIngredients() {
        List<RecipeIngredient> currentIngredients = new ArrayList<>();
        for (RecipeIngredient ingredient : ingredients) {
            // Créer une copie avec la quantité ajustée
            RecipeIngredient adjustedIngredient = new RecipeIngredient(
                    ingredient.getId(),
                    ingredient.getRecipeId(),
                    ingredient.getName(),
                    ingredient.getQuantity() * portionMultiplier,
                    ingredient.getUnit()
            );
            currentIngredients.add(adjustedIngredient);
        }
        return currentIngredients;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeIngredient ingredient = visibleIngredients.get(position);
        double adjustedQuantity = ingredient.getQuantity() * portionMultiplier;
        String quantity = formatQuantity(adjustedQuantity, ingredient.getUnit());

        holder.ingredientText.setText(String.format("%s %s %s",
                quantity, ingredient.getUnit(), ingredient.getName()));
    }
    private void updateVisibleIngredients() {
        visibleIngredients.clear();
        for (RecipeIngredient ingredient : ingredients) {
            double adjustedQuantity = ingredient.getQuantity() * portionMultiplier;
            int roundedQuantity = Math.round((float) adjustedQuantity);
            // N'ajouter l'ingrédient que si sa quantité est supérieure à 0
            if (roundedQuantity > 0) {
                visibleIngredients.add(ingredient);
            }
        }
    }

    private String formatQuantity(double quantity, String unit) {
        // Pour les unités qui nécessitent des nombres entiers
        if (unit.equalsIgnoreCase("unité") ||
                unit.equalsIgnoreCase("") ||
                unit.equalsIgnoreCase("pincée") ||
                unit.equalsIgnoreCase("pincées") ||
                unit.equalsIgnoreCase("g") ||
                unit.equalsIgnoreCase("c. à soupe") ||
                unit.equalsIgnoreCase("c. à café") ||
                unit.equalsIgnoreCase("ml") ||
                unit.equalsIgnoreCase("tranche") ||
                unit.equalsIgnoreCase("tranches") ||
                unit.equalsIgnoreCase("rouleau") ||
                unit.equalsIgnoreCase("pot") ||
                unit.equalsIgnoreCase("pots") ||
                unit.equalsIgnoreCase("sachet") ||
                unit.equalsIgnoreCase("poignée") ||
                unit.equalsIgnoreCase("poignées") ||
                unit.equalsIgnoreCase("oeuf") ||
                unit.equalsIgnoreCase("oeufs") ||
                unit.equalsIgnoreCase("gousse") ||
                unit.equalsIgnoreCase("gousses")) {

            return String.valueOf(Math.round(quantity));
        }  // Pour les autres unités (g, ml, etc.), garder une décimale
        return String.format(Locale.getDefault(), "%.1f", quantity);
    }


    @Override
    public int getItemCount() {
        return visibleIngredients.size();
    }

    public void updatePortions(double multiplier) {
        this.portionMultiplier = multiplier;
        updateVisibleIngredients();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView ingredientText;

        ViewHolder(View view) {
            super(view);
            ingredientText = view.findViewById(R.id.ingredientText);
        }
    }
}
package com.example.recipeapp.ui;

import android.content.Context;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.RecipeIngredient;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {
    private List<RecipeIngredient> ingredients;
    private DatabaseHelper dbHelper;
    private Context context;
    private Set<Long> checkedItems;

    public ShoppingListAdapter(Context context, List<RecipeIngredient> ingredients) {
        this.context = context;
        this.ingredients = ingredients;
        this.dbHelper = new DatabaseHelper(context);
        this.checkedItems = new HashSet<>();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view);
    }

    public void sortByRecipe() {
        Collections.sort(ingredients, (i1, i2) -> {
            String recipe1 = i1.getRecipeName() != null ? i1.getRecipeName() : "";
            String recipe2 = i2.getRecipeName() != null ? i2.getRecipeName() : "";
            return recipe1.compareToIgnoreCase(recipe2);
        });
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeIngredient ingredient = ingredients.get(position);

        // Supprimer les anciens listeners pour éviter les doublons
        if (holder.quantityEdit.getTag() instanceof TextWatcher) {
            holder.quantityEdit.removeTextChangedListener((TextWatcher) holder.quantityEdit.getTag());
        }

        // Affichage initial
        String formattedQuantity = formatQuantityWithUnit(ingredient.getQuantity(), ingredient.getUnit());
        String[] parts = formattedQuantity.split(" ");
        holder.quantityEdit.setText(parts[0]);
        if (parts.length > 1) {
            holder.unitText.setText(parts[1]);
            ingredient.setUnit(parts[1]);
        } else {
            holder.unitText.setText(ingredient.getUnit());
        }

        // Configuration de la checkbox (un seul listener)
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(false);
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    dbHelper.deleteFromShoppingList(ingredient.getId());
                    ingredients.remove(adapterPosition);
                    notifyItemRemoved(adapterPosition);
                    notifyItemRangeChanged(adapterPosition, getItemCount());
                }
            }
        });

        // TextWatcher pour la quantité
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().equals("0")) {
                    holder.quantityEdit.setText("1");
                    holder.quantityEdit.setSelection(1);
                    return;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().isEmpty()) return;

                try {
                    String quantityText = s.toString().replace(",", ".").trim();
                    double newQuantity = Double.parseDouble(quantityText);

                    if (newQuantity == 0) {
                        holder.quantityEdit.setText("1");
                        holder.quantityEdit.setSelection(1);
                        return;
                    }

                    if (newQuantity > 9999) {
                        newQuantity = 9999;
                        holder.quantityEdit.setText(String.valueOf(newQuantity));
                        holder.quantityEdit.setSelection(holder.quantityEdit.length());
                    }

                    String currentUnit = ingredient.getUnit();

                    // Gérer les conversions
                    if (currentUnit.equalsIgnoreCase("g") && newQuantity >= 1000) {
                        newQuantity = newQuantity / 1000;
                        currentUnit = "kg";
                        // Mettre à jour l'affichage immédiatement
                        holder.quantityEdit.setText(String.format(Locale.getDefault(), "%.1f", newQuantity));
                        holder.unitText.setText(currentUnit);
                        holder.quantityEdit.setSelection(holder.quantityEdit.length());
                    } else if (currentUnit.equalsIgnoreCase("ml") && newQuantity >= 1000) {
                        newQuantity = newQuantity / 1000;
                        currentUnit = "L";
                        // Mettre à jour l'affichage immédiatement
                        holder.quantityEdit.setText(String.format(Locale.getDefault(), "%.1f", newQuantity));
                        holder.unitText.setText(currentUnit);
                        holder.quantityEdit.setSelection(holder.quantityEdit.length());
                    }

                    if (newQuantity > 0) {
                        ingredient.setQuantity(newQuantity);
                        ingredient.setUnit(currentUnit);
                        dbHelper.updateShoppingListQuantity(ingredient.getId(), newQuantity, currentUnit);
                    }


                } catch (NumberFormatException e) {
                    holder.quantityEdit.setText(String.valueOf(Math.round(ingredient.getQuantity())));
                }
            }
        };


        holder.quantityEdit.setTag(textWatcher);
        holder.quantityEdit.addTextChangedListener(textWatcher);

        // Limiter à 4 chiffres
        holder.quantityEdit.setFilters(new InputFilter[]{
                new InputFilter.LengthFilter(4)
        });

        // Configuration des autres éléments
        holder.ingredientText.setText(ingredient.getName());

        // Bouton de suppression
        holder.deleteButton.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION) {
                dbHelper.deleteFromShoppingList(ingredient.getId());
                ingredients.remove(adapterPosition);
                notifyItemRemoved(adapterPosition);
                notifyItemRangeChanged(adapterPosition, getItemCount());
            }
        });
    }

    private void updateQuantity(ViewHolder holder, RecipeIngredient ingredient) {
        try {
            String quantityText = holder.quantityEdit.getText().toString()
                    .replace(",", ".")
                    .trim();
            double newQuantity = Double.parseDouble(quantityText);

            // Vérifier si la quantité est valide
            if (newQuantity > 0) {
                ingredient.setQuantity(newQuantity);
                dbHelper.updateShoppingListQuantity(ingredient.getId(), newQuantity);

                // Reformater l'affichage
                holder.quantityEdit.setText(formatQuantityWithUnit(newQuantity, ingredient.getUnit()));
            } else {
                // Réinitialiser à la valeur précédente si invalide
                holder.quantityEdit.setText(formatQuantityWithUnit(ingredient.getQuantity(), ingredient.getUnit()));
            }
        } catch (NumberFormatException e) {
            // En cas d'erreur, réinitialiser à la valeur précédente
            holder.quantityEdit.setText(formatQuantityWithUnit(ingredient.getQuantity(), ingredient.getUnit()));
        }
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    private String formatQuantityWithUnit(double quantity, String unit) {
        // Conversion et formatage pour les grammes
        if (unit.equalsIgnoreCase("g") && quantity >= 1000) {
            return String.format(Locale.getDefault(), "%.1f kg", quantity / 1000);
        }

        // Conversion et formatage pour les millilitres/litres
        if (unit.equalsIgnoreCase("ml") || unit.equalsIgnoreCase("L") ||
                unit.equalsIgnoreCase("l") || unit.equalsIgnoreCase("litre") ||
                unit.equalsIgnoreCase("litres")) {

            if (unit.equalsIgnoreCase("ml") && quantity >= 1000) {
                return String.format(Locale.getDefault(), "%.1f L", quantity / 1000);
            } else if (unit.equalsIgnoreCase("ml")) {
                return String.format(Locale.getDefault(), "%d ml", Math.round(quantity));
            } else {
                // Si déjà en litres
                return String.format(Locale.getDefault(), "%.1f L", quantity);
            }
        }

        // Format standard pour les autres cas avec nombres entiers
        if (unit.equalsIgnoreCase("unité") ||
                unit.equalsIgnoreCase("") ||
                unit.equalsIgnoreCase("pincée") ||
                unit.equalsIgnoreCase("pincées") ||
                unit.equalsIgnoreCase("g") ||
                unit.equalsIgnoreCase("c. à soupe") ||
                unit.equalsIgnoreCase("c. à café") ||
                unit.equalsIgnoreCase("tranche") ||
                unit.equalsIgnoreCase("tranches")) {
            return String.format(Locale.getDefault(), "%d %s", Math.round(quantity), unit);
        }

        // Format avec décimale pour les autres cas
        return String.format(Locale.getDefault(), "%.1f %s", quantity, unit);
    }

    public void sortByCategory() {
        // À implémenter si vous avez une catégorie dans vos ingrédients
        notifyDataSetChanged();
    }

    public void sortAlphabetically() {
        Collections.sort(ingredients, (i1, i2) ->
                i1.getName().compareToIgnoreCase(i2.getName()));
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        EditText quantityEdit;
        TextView unitText;
        TextView ingredientText;
        ImageButton deleteButton;

        ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkBox);
            quantityEdit = view.findViewById(R.id.quantityEdit);
            unitText = view.findViewById(R.id.unitText);
            ingredientText = view.findViewById(R.id.ingredientText);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}
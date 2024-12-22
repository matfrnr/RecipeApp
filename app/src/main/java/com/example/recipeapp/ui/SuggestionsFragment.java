package com.example.recipeapp.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SuggestionsFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private TextInputEditText ingredientInput;
    private ChipGroup chipGroup;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextView resultText;
    private Set<String> selectedIngredients;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_suggestions, container, false);
        initializeViews(view);
        setupListeners();

        FloatingActionButton chatFab = view.findViewById(R.id.chatFab);
        chatFab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ChatActivity.class);
            startActivity(intent);
        });

        return view;
    }
    private void initializeViews(View view) {
        dbHelper = new DatabaseHelper(getContext());
        selectedIngredients = new HashSet<>();

        ingredientInput = view.findViewById(R.id.ingredientInput);
        chipGroup = view.findViewById(R.id.chipGroup);
        recyclerView = view.findViewById(R.id.suggestionsRecyclerView);
        resultText = view.findViewById(R.id.resultText);

        // Configuration du RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(getContext(), new ArrayList<>(), false);
        recyclerView.setAdapter(adapter);
    }

    private void setupListeners() {
        ingredientInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                addIngredientChip(ingredientInput.getText().toString().trim());
                return true;
            }
            return false;
        });
    }

    private void addIngredientChip(String ingredient) {
        if (ingredient.isEmpty() || selectedIngredients.contains(ingredient.toLowerCase())) {
            return;
        }

        Chip chip = new Chip(requireContext());
        chip.setText(ingredient);
        chip.setCloseIconVisible(true);
        chip.setCheckable(false);

        chip.setOnCloseIconClickListener(v -> {
            chipGroup.removeView(chip);
            selectedIngredients.remove(ingredient.toLowerCase());
            updateSuggestions();
        });

        chipGroup.addView(chip);
        selectedIngredients.add(ingredient.toLowerCase());
        ingredientInput.setText("");

        updateSuggestions();
    }

    private void updateSuggestions() {
        if (selectedIngredients.isEmpty()) {
            resultText.setText("Ajoutez des ingrédients pour voir les suggestions");
            adapter.updateRecipes(new ArrayList<>());
            return;
        }

        List<Recipe> suggestions = dbHelper.findRecipesByIngredients(new ArrayList<>(selectedIngredients));

        if (suggestions.isEmpty()) {
            resultText.setText("Aucune recette trouvée avec ces ingrédients");
        } else {
            resultText.setText(String.format("Recettes trouvées (%d)", suggestions.size()));
        }

        adapter.updateRecipes(suggestions);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
package com.example.recipeapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.UserPreferences;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class CommunityRecipesFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private TextView emptyView;
    private boolean isReceiverRegistered = false;


    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        @androidx.annotation.RequiresApi(api = android.os.Build.VERSION_CODES.TIRAMISU)
        public void onReceive(Context context, Intent intent) {
            if ("com.example.recipeapp.RECIPE_UPDATED".equals(intent.getAction())) {
                loadCommunityRecipes();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_community_recipes, container, false);

        dbHelper = new DatabaseHelper(getContext());
        setupViews(view);
        loadCommunityRecipes();

        return view;
    }

    private void setupViews(View view) {
        recyclerView = view.findViewById(R.id.communityRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        FloatingActionButton addButton = view.findViewById(R.id.addRecipeButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(getContext(), new ArrayList<>(), true);
        recyclerView.setAdapter(adapter);

        addButton.setOnClickListener(v -> {
            SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            if (prefs.getInt("userId", -1) != -1) {
                startActivity(new Intent(getContext(), CreateRecipeActivity.class));
            } else {
                Toast.makeText(getContext(), "Connectez-vous pour ajouter une recette", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadCommunityRecipes() {
        // Récupérer les préférences utilisateur
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        UserPreferences userPrefs = null;
        if (userId != -1) {
            userPrefs = dbHelper.getUserPreferences(userId);
        }

        // Récupérer les recettes communautaires
        List<Recipe> communityRecipes = dbHelper.getCommunityRecipes();
        List<Recipe> filteredRecipes = new ArrayList<>();

        // Appliquer les filtres
        for (Recipe recipe : communityRecipes) {
            boolean isCompatible = true;

            // Vérifier les préférences alimentaires
            if (userPrefs != null && userPrefs.getDietType() != null && !userPrefs.getDietType().equals("none")) {
                String recipeDietType = recipe.getDietType();

                if (recipeDietType != null) {
                    // Gérer les cas spéciaux de compatibilité
                    switch (userPrefs.getDietType()) {
                        case "vegetarian":
                            isCompatible = recipeDietType.equals("vegetarian") ||
                                    recipeDietType.equals("vegan");
                            break;
                        case "vegan":
                            isCompatible = recipeDietType.equals("vegan");
                            break;
                        case "gluten_free":
                            isCompatible = recipeDietType.equals("gluten_free");
                            break;
                        default:
                            isCompatible = true;
                            break;
                    }
                } else {
                    isCompatible = false;  // Si la recette n'a pas de type de régime défini
                }
            }

            // Vérifier les allergies
            if (isCompatible && userPrefs != null && userPrefs.getAllergies() != null && !userPrefs.getAllergies().isEmpty()) {
                List<String> userAllergies = userPrefs.getAllergies();
                List<String> recipeAllergens = recipe.getAllergens();

                if (recipeAllergens != null) {
                    for (String allergy : userAllergies) {
                        if (recipeAllergens.contains(allergy.toLowerCase())) {
                            isCompatible = false;
                            break;
                        }
                    }
                }
            }

            if (isCompatible) {
                filteredRecipes.add(recipe);
            }
        }

        adapter.updateRecipes(filteredRecipes);

        if (filteredRecipes.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isReceiverRegistered) {
            IntentFilter filter = new IntentFilter("com.example.recipeapp.RECIPE_UPDATED");
            requireContext().registerReceiver(updateReceiver, filter);
            isReceiverRegistered = true;
        }
        loadCommunityRecipes();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isReceiverRegistered) {
            requireContext().unregisterReceiver(updateReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}

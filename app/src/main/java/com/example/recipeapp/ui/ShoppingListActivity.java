package com.example.recipeapp.ui;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.RecipeIngredient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
public class ShoppingListActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private ShoppingListAdapter adapter;
    private TextView emptyView;
    private FloatingActionButton clearButton; // Changé de Button à FloatingActionButton
    private FloatingActionButton addFab;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        dbHelper = new DatabaseHelper(this);
        emptyView = findViewById(R.id.emptyView);
        clearButton = findViewById(R.id.clearButton); // Maintenant c'est un FAB
        addFab = findViewById(R.id.addIngredientFab);

        setupToolbar();
        setupRecyclerView();
        setupButtons();
        loadShoppingList();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Liste de courses");
        }
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.shoppingListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupAddButton() {
        FloatingActionButton addFab = findViewById(R.id.addIngredientFab);
        addFab.setOnClickListener(v -> showAddIngredientDialog());
    }

    private void showAddIngredientDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_ingredient, null);

        EditText ingredientNameInput = dialogView.findViewById(R.id.ingredientNameInput);
        EditText quantityInput = dialogView.findViewById(R.id.quantityInput);
        Spinner unitSpinner = dialogView.findViewById(R.id.unitSpinner);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"unité(s)", "g", "ml", "litre(s)", "pot(s)",
                        "kg(s)", "tranche(s)", "sachet(s)"});
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);
        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String name = ingredientNameInput.getText().toString().trim();
            String quantityStr = quantityInput.getText().toString()
                    .replace(",", ".")
                    .trim();
            String unit = unitSpinner.getSelectedItem().toString();

            if (!name.isEmpty() && !quantityStr.isEmpty()) {
                try {
                    double quantity = Double.parseDouble(quantityStr);
                    if (quantity > 0) {
                        // Convertir les grandes quantités si nécessaire
                        if (unit.equals("g") && quantity >= 1000) {
                            quantity = quantity / 1000;
                            unit = "kg";
                        } else if (unit.equals("ml") && quantity >= 1000) {
                            quantity = quantity / 1000;
                            unit = "L";
                        } else if (unit.equals("litre") || unit.equals("litres")) {
                            unit = "L"; // Normaliser l'unité
                        }

                        RecipeIngredient ingredient = new RecipeIngredient(0, 0, name, quantity, unit);
                        List<RecipeIngredient> ingredients = new ArrayList<>();
                        ingredients.add(ingredient);
                        dbHelper.addToShoppingList("Ajout manuel", ingredients);
                        loadShoppingList();
                    } else {
                        Toast.makeText(this, "La quantité doit être supérieure à 0",
                                Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Quantité invalide",
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setView(dialogView)
                .setTitle("Ajouter un ingrédient")
                .setPositiveButton("Ajouter", (dialog, which) -> {
                    String name = ingredientNameInput.getText().toString();
                    String quantityStr = quantityInput.getText().toString()
                            .replace(",", ".")
                            .trim();
                    String unit = unitSpinner.getSelectedItem().toString();

                    if (!name.isEmpty() && !quantityStr.isEmpty()) {
                        try {
                            double quantity = Double.parseDouble(quantityStr);
                            if (quantity > 0) {
                                RecipeIngredient ingredient = new RecipeIngredient(0, 0, name, quantity, unit);
                                List<RecipeIngredient> ingredients = new ArrayList<>();
                                ingredients.add(ingredient);
                                dbHelper.addToShoppingList("Ajout manuel", ingredients);
                                loadShoppingList();
                            } else {
                                Toast.makeText(this, "La quantité doit être supérieure à 0",
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(this, "Quantité invalide",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Annuler", null);

        builder.create().show();
    }

    private void loadShoppingList() {
        List<RecipeIngredient> shoppingList = dbHelper.getShoppingList();
        adapter = new ShoppingListAdapter(this, shoppingList);
        recyclerView.setAdapter(adapter);

        // Gérer la visibilité des éléments
        if (shoppingList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
            clearButton.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            clearButton.setVisibility(View.VISIBLE);
        }
    }

    private void setupButtons() {
        addFab.setOnClickListener(v -> showAddIngredientDialog());

        clearButton.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Vider la liste")
                    .setMessage("Êtes-vous sûr de vouloir vider la liste ?")
                    .setPositiveButton("Oui", (dialog, which) -> {
                        dbHelper.clearShoppingList();
                        loadShoppingList();
                        Toast.makeText(this, "Liste vidée", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Non", null)
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
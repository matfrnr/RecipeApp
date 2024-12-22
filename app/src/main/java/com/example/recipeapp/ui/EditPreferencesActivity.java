package com.example.recipeapp.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.UserPreferences;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditPreferencesActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private AutoCompleteTextView allergySpinner;
    private ChipGroup selectedAllergiesChipGroup;
    private RadioGroup dietTypeGroup;
    private Set<String> selectedAllergies = new HashSet<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_preferences);

        // Configuration de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Préférences alimentaires");
        }

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupAllergySpinner();
        loadUserPreferences();
        setupListeners();
    }

    private void setupAllergySpinner() {
        String[] allergies = new String[]{
                "Lactose", "Gluten", "Arachides", "Fruits à coque",
                "Soja", "Œufs", "Poisson", "Crustacés",
                "Céleri", "Moutarde", "Sésame", "Sulfites"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                allergies
        );
        allergySpinner.setAdapter(adapter);
        allergySpinner.setOnItemClickListener((parent, view, position, id) -> {
            String selectedAllergy = adapter.getItem(position);
            if (selectedAllergy != null) {
                addAllergyChip(selectedAllergy);
                allergySpinner.setText("");
            }
        });
    }

    private void initializeViews() {
        allergySpinner = findViewById(R.id.allergySpinner);
        selectedAllergiesChipGroup = findViewById(R.id.selectedAllergiesChipGroup);
        dietTypeGroup = findViewById(R.id.dietTypeGroup);
    }


    private void loadUserPreferences() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            UserPreferences preferences = dbHelper.getUserPreferences(userId);

            // Charger les allergies
            selectedAllergiesChipGroup.removeAllViews();
            for (String allergy : preferences.getAllergies()) {
                addAllergyChip(allergy);
            }

            String dietType = preferences.getDietType();
            if (dietType != null) {
                switch (dietType) {
                    case "vegetarian":
                        dietTypeGroup.check(R.id.dietVegetarian);
                        break;
                    case "vegan":
                        dietTypeGroup.check(R.id.dietVegan);
                        break;
                    case "gluten_free":
                        dietTypeGroup.check(R.id.dietGlutenFree);
                        break;
                    default:
                        dietTypeGroup.check(R.id.dietNone);
                        break;
                }
            }
        }
    }

    private void setupListeners() {
        // Bouton de sauvegarde
        findViewById(R.id.saveButton).setOnClickListener(v -> savePreferences());
    }


    private void addAllergyChip(String allergy) {
        if (!selectedAllergies.contains(allergy.toLowerCase())) {
            Chip chip = new Chip(this);
            chip.setText(allergy);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedAllergiesChipGroup.removeView(chip);
                selectedAllergies.remove(allergy.toLowerCase());
            });
            selectedAllergiesChipGroup.addView(chip);
            selectedAllergies.add(allergy.toLowerCase());
        }
    }

    private void savePreferences() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            // Récupérer le régime sélectionné
            String dietType;
            int checkedId = dietTypeGroup.getCheckedRadioButtonId();
            if (checkedId == R.id.dietVegetarian) dietType = "vegetarian";
            else if (checkedId == R.id.dietVegan) dietType = "vegan";
            else if (checkedId == R.id.dietGlutenFree) dietType = "gluten_free";
            else dietType = "none";

            // Créer l'objet préférences
            UserPreferences preferences = new UserPreferences();
            preferences.setDietType(dietType);
            preferences.setAllergies(new ArrayList<>(selectedAllergies));

            // Sauvegarder
            dbHelper.saveUserPreferences(userId, preferences);

            Toast.makeText(this, "Préférences enregistrées", Toast.LENGTH_SHORT).show();
            finish();
        }
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
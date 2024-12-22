package com.example.recipeapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class EditRecipeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private EditText nameInput, descriptionInput, stepsInput, cookingTimeInput, servingsInput;
    private Spinner difficultySpinner;
    private ImageView recipeImage;
    private DatabaseHelper dbHelper;
    private Recipe recipe;
    private String selectedImagePath;
    private AutoCompleteTextView allergySpinner;
    private ChipGroup selectedAllergensChipGroup;
    private Set<String> selectedAllergens = new HashSet<>();
    private Spinner dietTypeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Modifier la recette");

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupAllergySpinner();
        setupDietTypeSpinner();

        // Récupérer et afficher la recette
        long recipeId = getIntent().getLongExtra("recipe_id", -1);
        if (recipeId != -1) {
            recipe = dbHelper.getRecipe((int) recipeId);
            if (recipe != null) {
                fillFormWithRecipeData();
            }
        }
    }

    private void setupDietTypeSpinner() {
        dietTypeSpinner = findViewById(R.id.dietTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.diet_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietTypeSpinner.setAdapter(adapter);
    }

    private void setupAllergySpinner() {
        // Même code que dans CreateRecipeActivity
        allergySpinner = findViewById(R.id.allergySpinner);
        selectedAllergensChipGroup = findViewById(R.id.selectedAllergensChipGroup);

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

    private void addAllergyChip(String allergy) {
        if (!selectedAllergens.contains(allergy.toLowerCase())) {
            Chip chip = new Chip(this);
            chip.setText(allergy);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(v -> {
                selectedAllergensChipGroup.removeView(chip);
                selectedAllergens.remove(allergy.toLowerCase());
            });
            selectedAllergensChipGroup.addView(chip);
            selectedAllergens.add(allergy.toLowerCase());
        }
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        stepsInput = findViewById(R.id.stepsInput);
        cookingTimeInput = findViewById(R.id.cookingTimeInput);
        servingsInput = findViewById(R.id.servingsInput);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        recipeImage = findViewById(R.id.recipeImage);

        Button addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(v -> openGallery());

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveRecipe());
    }

    private void fillFormWithRecipeData() {
        nameInput.setText(recipe.getName());
        descriptionInput.setText(recipe.getDescription());
        stepsInput.setText(recipe.getSteps());
        cookingTimeInput.setText(String.valueOf(recipe.getCookingTime()));
        servingsInput.setText(String.valueOf(recipe.getServings()));

        // Définir la difficulté dans le spinner
        String[] difficulties = getResources().getStringArray(R.array.difficulty_levels);
        for (int i = 0; i < difficulties.length; i++) {
            if (difficulties[i].equals(recipe.getDifficulty())) {
                difficultySpinner.setSelection(i);
                break;
            }
        }

        String dietType = recipe.getDietType();
        String[] dietTypes = getResources().getStringArray(R.array.diet_types);
        for (int i = 0; i < dietTypes.length; i++) {
            String currentDietType = dietTypes[i];
            if ((currentDietType.equals("Végétarien") && dietType.equals("vegetarian")) ||
                    (currentDietType.equals("Végétalien") && dietType.equals("vegan")) ||
                    (currentDietType.equals("Sans gluten") && dietType.equals("gluten_free")) ||
                    (currentDietType.equals("Aucun régime spécial") && dietType.equals("none"))) {
                dietTypeSpinner.setSelection(i);
                break;
            }
        }
        // Remplir les allergènes
        selectedAllergensChipGroup.removeAllViews();
        selectedAllergens.clear();
        for (String allergen : recipe.getAllergens()) {
            addAllergyChip(allergen);
        }

        // Charger l'image
        selectedImagePath = recipe.getImageUrl();
        if (selectedImagePath != null && !selectedImagePath.isEmpty()) {
            if (selectedImagePath.startsWith("@drawable/")) {
                int resourceId = getResources().getIdentifier(
                        selectedImagePath.replace("@drawable/", ""),
                        "drawable",
                        getPackageName()
                );
                if (resourceId != 0) {
                    recipeImage.setImageResource(resourceId);
                }
            } else {
                Uri imageUri = Uri.parse(selectedImagePath);
                recipeImage.setImageURI(imageUri);
            }
        }
    }

    private void openGallery() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE);
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de l'ouverture de la galerie", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    // Sauvegarder dans le stockage interne
                    String fileName = "recipe_" + recipe.getId() + ".jpg";
                    File internalFile = new File(getFilesDir(), fileName);

                    // Copier l'image
                    InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                    FileOutputStream outputStream = new FileOutputStream(internalFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();

                    // Mettre à jour le chemin de l'image dans la recette
                    selectedImagePath = internalFile.getAbsolutePath();
                    recipeImage.setImageURI(Uri.fromFile(internalFile));
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erreur lors de la sélection de l'image", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    private void saveRecipe() {
        if (nameInput.getText().toString().isEmpty() ||
                descriptionInput.getText().toString().isEmpty() ||
                stepsInput.getText().toString().isEmpty() ||
                cookingTimeInput.getText().toString().isEmpty() ||
                servingsInput.getText().toString().isEmpty()) {

            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            recipe.setName(nameInput.getText().toString());
            recipe.setDescription(descriptionInput.getText().toString());
            recipe.setSteps(stepsInput.getText().toString());
            recipe.setCookingTime(Integer.parseInt(cookingTimeInput.getText().toString()));
            recipe.setServings(Integer.parseInt(servingsInput.getText().toString()));
            recipe.setDifficulty(difficultySpinner.getSelectedItem().toString());

            String selectedDietType = dietTypeSpinner.getSelectedItem().toString();
            switch (selectedDietType) {
                case "Végétarien":
                    recipe.setDietType("vegetarian");
                    break;
                case "Végétalien":
                    recipe.setDietType("vegan");
                    break;
                case "Sans gluten":
                    recipe.setDietType("gluten_free");
                    break;
                default:
                    recipe.setDietType("none");
                    break;
            }
            recipe.setAllergens(new ArrayList<>(selectedAllergens));

            if (selectedImagePath != null) {
                recipe.setImageUrl(selectedImagePath);
            }

            dbHelper.updateRecipe(recipe);
            sendBroadcast(new Intent("RECIPE_UPDATED"));
            Toast.makeText(this, "Recette mise à jour", Toast.LENGTH_SHORT).show();
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valeurs numériques invalides", Toast.LENGTH_SHORT).show();
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
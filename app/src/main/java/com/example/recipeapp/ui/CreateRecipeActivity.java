package com.example.recipeapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.inputmethod.EditorInfo;
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
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CreateRecipeActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private EditText nameInput, descriptionInput, stepsInput, cookingTimeInput, servingsInput;
    private Spinner difficultySpinner;
    private ImageView recipeImage;
    private DatabaseHelper dbHelper;
    private String selectedImagePath;
    private Spinner dietTypeSpinner;
    private ChipGroup allergensChipGroup;
    private TextInputEditText allergenInput;
    private AutoCompleteTextView allergySpinner;
    private ChipGroup selectedAllergensChipGroup;
    private Set<String> selectedAllergens = new HashSet<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_recipe);

        // Configuration de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Créer une recette");
        setupDietTypeSpinner();
        setupAllergySpinner();
        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupListeners();
    }

    private void setupAllergySpinner() {
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


    private void setupDietTypeSpinner() {
        dietTypeSpinner = findViewById(R.id.dietTypeSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.diet_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dietTypeSpinner.setAdapter(adapter);
    }

    private void addAllergenChip(String allergen) {
        Chip chip = new Chip(this);
        chip.setText(allergen);
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> {
            allergensChipGroup.removeView(chip);
            selectedAllergens.remove(allergen.toLowerCase());
        });
        allergensChipGroup.addView(chip);
        selectedAllergens.add(allergen.toLowerCase());
    }

    private void initializeViews() {
        nameInput = findViewById(R.id.nameInput);
        descriptionInput = findViewById(R.id.descriptionInput);
        stepsInput = findViewById(R.id.stepsInput);
        cookingTimeInput = findViewById(R.id.cookingTimeInput);
        servingsInput = findViewById(R.id.servingsInput);
        difficultySpinner = findViewById(R.id.difficultySpinner);
        recipeImage = findViewById(R.id.recipeImage);
    }

    private void setupListeners() {
        Button addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(v -> openGallery());

        Button submitButton = findViewById(R.id.submitButton);
        submitButton.setOnClickListener(v -> saveRecipe());
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri imageUri = data.getData();
            String savedImagePath = saveImage(imageUri);
            if (savedImagePath != null) {
                selectedImagePath = savedImagePath;
                recipeImage.setImageURI(Uri.parse(selectedImagePath));
            } else {
                Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveRecipe() {
        // Vérifier que les champs obligatoires sont remplis
        if (nameInput.getText().toString().isEmpty() ||
                descriptionInput.getText().toString().isEmpty() ||
                stepsInput.getText().toString().isEmpty() ||
                cookingTimeInput.getText().toString().isEmpty() ||
                servingsInput.getText().toString().isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        String username = prefs.getString("username", "Anonyme");

        if (userId == -1) {
            Toast.makeText(this, "Vous devez être connecté pour créer une recette", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Recipe recipe = new Recipe(
                    0,
                    nameInput.getText().toString(),
                    descriptionInput.getText().toString(),
                    stepsInput.getText().toString(),
                    selectedImagePath != null ? selectedImagePath : "@drawable/default_recipe_image",
                    Integer.parseInt(cookingTimeInput.getText().toString()),
                    difficultySpinner.getSelectedItem().toString(),
                    Integer.parseInt(servingsInput.getText().toString()),
                    username,
                    userId
            );

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

            long recipeId = dbHelper.createCommunityRecipe(recipe, userId);
            if (recipeId != -1) {
                Toast.makeText(this, "Recette créée avec succès", Toast.LENGTH_SHORT).show();
                // Envoyer le broadcast
                Intent intent = new Intent("com.example.recipeapp.RECIPE_UPDATED")
                        .setPackage(getPackageName());
                sendBroadcast(intent);
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la création de la recette", Toast.LENGTH_SHORT).show();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Veuillez saisir des valeurs numériques valides", Toast.LENGTH_SHORT).show();
        }
    }

    private String saveImage(Uri imageUri) {
        try {
            String fileName = "recipe_" + System.currentTimeMillis() + ".jpg";
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            File file = new File(getFilesDir(), fileName);

            FileOutputStream outputStream = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
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
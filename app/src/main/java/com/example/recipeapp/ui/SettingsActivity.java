package com.example.recipeapp.ui;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.recipeapp.R;
import com.google.android.material.card.MaterialCardView;
import android.content.Intent;
import com.example.recipeapp.ui.EditPreferencesActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Configuration de la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Paramètres");
        }

        setupButtons(); // Appeler setupButtons ici
        setupThemeSwitch(); // Ajouter la configuration pour le Switch du thème
    }

    private void setupButtons() {
        // Bouton des préférences alimentaires
        MaterialCardView dietaryPreferencesCard = findViewById(R.id.dietaryPreferencesButton);
        dietaryPreferencesCard.setOnClickListener(v -> {
            Toast.makeText(this, "Ouverture des préférences...", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, EditPreferencesActivity.class);
            startActivity(intent);
        });
    }

    private void setupThemeSwitch() {
        Switch themeSwitch = findViewById(R.id.themeSwitch);

        // Charger la préférence enregistrée pour le mode nuit
        SharedPreferences prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        boolean isNightMode = prefs.getBoolean("night_mode", false);
        themeSwitch.setChecked(isNightMode);

        themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Enregistrer la préférence et appliquer le thème
            ThemeManager.saveThemePreference(this, isChecked);
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );

            // Redémarrer l'activité pour appliquer le changement immédiatement
            recreate();
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}

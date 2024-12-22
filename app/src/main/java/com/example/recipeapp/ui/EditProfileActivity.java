package com.example.recipeapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;

public class EditProfileActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private EditText usernameInput, emailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Configuration de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Modifier le profil");
        }

        dbHelper = new DatabaseHelper(this);
        initializeViews();
        loadUserData();
        setupSaveButton();
    }

    private void initializeViews() {
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        usernameInput.setText(prefs.getString("username", ""));
        emailInput.setText(prefs.getString("email", ""));
    }

    private void setupSaveButton() {
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String newUsername = usernameInput.getText().toString().trim();
        String newEmail = emailInput.getText().toString().trim();

        if (newUsername.isEmpty() || newEmail.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            if (dbHelper.updateUserProfile(userId, newUsername, newEmail)) {
                // Mettre à jour les SharedPreferences
                prefs.edit()
                        .putString("username", newUsername)
                        .putString("email", newEmail)
                        .apply();

                // Envoyer le broadcast
                Intent intent = new Intent("com.example.recipeapp.PROFILE_UPDATED");
                sendBroadcast(intent);

                Toast.makeText(this, "Profil mis à jour avec succès", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Erreur lors de la mise à jour du profil", Toast.LENGTH_SHORT).show();
            }
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
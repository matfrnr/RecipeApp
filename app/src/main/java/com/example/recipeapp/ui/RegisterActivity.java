package com.example.recipeapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;

public class RegisterActivity extends AppCompatActivity {
    private EditText usernameInput, emailInput, passwordInput;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        dbHelper = new DatabaseHelper(this);
        usernameInput = findViewById(R.id.usernameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);

        findViewById(R.id.registerButton).setOnClickListener(v -> attemptRegister());
    }

    private void attemptRegister() {
        String username = usernameInput.getText().toString();
        String email = emailInput.getText().toString();
        String password = passwordInput.getText().toString();

        // Vérifications basiques
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            return;
        }

        long userId = dbHelper.registerUser(username, email, password);
        if (userId != -1) {
            // Sauvegarder les informations de l'utilisateur dans les SharedPreferences
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            prefs.edit()
                    .putInt("userId", (int)userId)
                    .putString("username", username)
                    .putString("email", email)
                    .apply();

            Toast.makeText(this, "Inscription réussie", Toast.LENGTH_SHORT).show();
            // Rediriger directement vers MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Pour effacer la pile d'activités
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "L'inscription a échoué", Toast.LENGTH_SHORT).show();
        }
    }
}
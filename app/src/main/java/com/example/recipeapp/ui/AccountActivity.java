package com.example.recipeapp.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class AccountActivity extends AppCompatActivity {
    private static final int PICK_IMAGE = 1;
    private static final int REQUEST_CAMERA = 2;
    private ImageView profileImage;
    private TextView usernameText, emailText;
    private DatabaseHelper dbHelper;
    private static final int EDIT_PROFILE_REQUEST = 1;
    private BroadcastReceiver updateReceiver;

    private final BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("com.example.recipeapp.PROFILE_UPDATED".equals(intent.getAction())) {
                displayUserInfo();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        loadUserData();
        // Enregistrer le receiver
        IntentFilter filter = new IntentFilter("com.example.recipeapp.PROFILE_UPDATED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(profileUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(profileUpdateReceiver, filter);
        }
        // Rafraîchir les informations
        displayUserInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Désenregistrer le receiver
        try {
            unregisterReceiver(profileUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // Ignorer si le receiver n'était pas enregistré
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        dbHelper = new DatabaseHelper(this);
        // Initialize views et autres...
        initViews();
        setupBroadcastReceiver();
        loadUserData();
    }

    private void setupBroadcastReceiver() {
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                loadUserData(); // Recharger les données quand on reçoit le broadcast
            }
        };
    }

    private void initViews() {
        profileImage = findViewById(R.id.profileImage);
        usernameText = findViewById(R.id.usernameText);
        emailText = findViewById(R.id.emailText);
    }

    private void loadUserData() {
        // Forcer la lecture des nouvelles données des SharedPreferences
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String email = prefs.getString("email", "");

        // Mettre à jour l'UI sur le thread principal
        runOnUiThread(() -> {
            usernameText.setText(username);
            emailText.setText(email);
        });
    }

    private void displayUserInfo() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String email = prefs.getString("email", "");

        usernameText.setText(username);
        emailText.setText(email);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Enregistrer le receiver avec LocalBroadcastManager
        LocalBroadcastManager.getInstance(this).registerReceiver(
                updateReceiver,
                new IntentFilter("com.example.recipeapp.PROFILE_UPDATED")
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Désenregistrer le receiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(updateReceiver);
    }


    private void setupClickListeners() {
        findViewById(R.id.myFavoritesButton).setOnClickListener(v -> {
            startActivity(new Intent(this, FavoritesActivity.class));
        });

        findViewById(R.id.editProfileButton).setOnClickListener(v -> {
            Intent intent = new Intent(this, EditProfileActivity.class);
            startActivityForResult(intent, EDIT_PROFILE_REQUEST);
        });

        findViewById(R.id.changePasswordButton).setOnClickListener(v -> {
            startActivity(new Intent(this, ChangePasswordActivity.class));
        });

        findViewById(R.id.themeButton).setOnClickListener(v -> {
            startActivity(new Intent(this, SettingsActivity.class));
        });

        findViewById(R.id.myRecipesButton).setOnClickListener(v -> {
            startActivity(new Intent(this, UserRecipesActivity.class));
        });

        findViewById(R.id.logoutButton).setOnClickListener(v -> {
            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            finish();
        });
    }

    private void showImagePickerDialog() {
        String[] options = {"Prendre une photo", "Choisir depuis la galerie", "Annuler"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choisir une photo de profil");
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:
                    if (checkCameraPermission()) {
                        openCamera();
                    }
                    break;
                case 1:
                    if (checkGalleryPermission()) {
                        openGallery();
                    }
                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
    }

    private boolean checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQUEST_CAMERA);
            return false;
        }
        return true;
    }

    private boolean checkGalleryPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        PICK_IMAGE);
                return false;
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        PICK_IMAGE);
                return false;
            }
        }
        return true;
    }
    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA);
        }
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Sélectionner une image"), PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_CAMERA) {
                openCamera();
            } else if (requestCode == PICK_IMAGE) {
                openGallery();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            loadUserData();  // Recharger les données après modification
        }
    }


    private Uri getImageUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "ProfileImage", null);
        return Uri.parse(path);
    }

    private void saveProfileImage(Uri imageUri) {
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri);
            if (bitmap != null) {
                // Redimensionner l'image pour éviter les problèmes de mémoire
                Bitmap resizedBitmap = getResizedBitmap(bitmap, 500); // 500 est la taille maximale
                profileImage.setImageBitmap(resizedBitmap);

                // Sauvegarder l'image en interne
                String fileName = "profile_image.jpg";
                FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.close();

                // Sauvegarder le nom du fichier dans les SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit().putString("profileImageFileName", fileName).apply();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show();
        }
    }

    // Méthode pour récupérer le Bitmap depuis l'Uri
    private Bitmap getBitmapFromUri(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT < 28) {
                return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            } else {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode pour redimensionner le bitmap
    private Bitmap getResizedBitmap(Bitmap bitmap, int maxSize) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }

        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    private void loadProfileImage() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String fileName = prefs.getString("profileImageFileName", null);
        if (fileName != null) {
            try {
                FileInputStream fis = openFileInput(fileName);
                Bitmap bitmap = BitmapFactory.decodeStream(fis);
                fis.close();
                profileImage.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
                // Si l'image ne peut pas être chargée, on met l'image par défaut
                profileImage.setImageResource(R.drawable.ic_account);
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
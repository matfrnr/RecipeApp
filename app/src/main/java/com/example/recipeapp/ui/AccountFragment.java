package com.example.recipeapp.ui;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class AccountFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private TextView usernameText, emailText;
    private ImageView profileImage;
    private View rootView;
    private static final int PICK_IMAGE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_account, container, false);

        // Vérifier si l'utilisateur est connecté
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        if (prefs.getInt("userId", -1) == -1) {
            // Non connecté -> afficher la page de connexion
            startActivity(new Intent(getContext(), LoginActivity.class));
            return rootView;
        }

        initViews(rootView);
        displayUserInfo();
        setupClickListeners();

        return rootView;
    }

    private void initViews(View view) {
        profileImage = view.findViewById(R.id.profileImage);
        usernameText = view.findViewById(R.id.usernameText);
        emailText = view.findViewById(R.id.emailText);

        profileImage.setOnClickListener(v -> checkPermissionAndOpenGallery());

        // Restaurer l'image de profil si elle existe
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String imagePath = prefs.getString("profileImagePath", null);
        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                profileImage.setImageURI(Uri.fromFile(imageFile));
            } else {
                profileImage.setImageResource(R.drawable.ic_account);
            }
        }
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_MEDIA_IMAGES}, PICK_IMAGE);
            } else {
                openGallery();
            }
        } else {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PICK_IMAGE);
            } else {
                openGallery();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PICK_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(requireContext(), "Permission refusée", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void displayUserInfo() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String username = prefs.getString("username", "");
        String email = prefs.getString("email", "");

        // Mettre à jour l'interface utilisateur
        usernameText.setText(username);
        emailText.setText(email);
    }

    private final BroadcastReceiver profileUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            displayUserInfo(); // Recharger les informations quand on reçoit un broadcast
        }
    };

    public void onPause() {
        super.onPause();
        try {
            requireContext().unregisterReceiver(profileUpdateReceiver);
        } catch (IllegalArgumentException e) {
            // Ignorer si le receiver n'était pas enregistré
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE && data != null) {
            Uri sourceUri = data.getData();
            if (sourceUri != null) {
                try {
                    // Copier l'image dans le stockage interne
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(sourceUri);
                    File internalStorageDir = new File(requireContext().getFilesDir(), "profile_images");
                    if (!internalStorageDir.exists()) {
                        internalStorageDir.mkdirs();
                    }
                    File imageFile = new File(internalStorageDir, "profile.jpg");
                    FileOutputStream outputStream = new FileOutputStream(imageFile);

                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    inputStream.close();
                    outputStream.close();

                    // Sauvegarder le chemin interne de l'image
                    String internalPath = imageFile.getAbsolutePath();
                    SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putString("profileImagePath", internalPath).apply();

                    // Afficher l'image
                    profileImage.setImageURI(Uri.fromFile(imageFile));

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(requireContext(), "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    public void onResume() {
        super.onResume();
        // Enregistrer le receiver avec le bon filtre
        IntentFilter filter = new IntentFilter("com.example.recipeapp.PROFILE_UPDATED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(profileUpdateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            requireContext().registerReceiver(profileUpdateReceiver, filter);
        }
        // Recharger les informations
        displayUserInfo();
    }

    private void setupClickListeners() {
        View editProfileButton = rootView.findViewById(R.id.editProfileButton);
        View myFavoritesButton = rootView.findViewById(R.id.myFavoritesButton);
        View myRecipesButton = rootView.findViewById(R.id.myRecipesButton);
        View changePasswordButton = rootView.findViewById(R.id.changePasswordButton);
        View themeButton = rootView.findViewById(R.id.themeButton);
        View logoutButton = rootView.findViewById(R.id.logoutButton);
        editProfileButton.setOnClickListener(v -> startActivity(new Intent(getContext(), EditProfileActivity.class)));
        myFavoritesButton.setOnClickListener(v -> startActivity(new Intent(getContext(), FavoritesActivity.class)));
        myRecipesButton.setOnClickListener(v -> startActivity(new Intent(getContext(), UserRecipesActivity.class)));
        changePasswordButton.setOnClickListener(v -> startActivity(new Intent(getContext(), ChangePasswordActivity.class)));
        themeButton.setOnClickListener(v -> startActivity(new Intent(getContext(), SettingsActivity.class)));
        logoutButton.setOnClickListener(v -> {
            requireContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
            startActivity(new Intent(getContext(), LoginActivity.class));
            requireActivity().finish();
        });
    }
}
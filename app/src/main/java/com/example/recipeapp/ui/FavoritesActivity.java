package com.example.recipeapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private View emptyView;

    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        @androidx.annotation.RequiresApi(api = android.os.Build.VERSION_CODES.TIRAMISU)
        public void onReceive(Context context, Intent intent) {
            if ("com.example.recipeapp.RECIPE_UPDATED".equals(intent.getAction())) {
                runOnUiThread(() -> {
                    loadFavorites();
                });
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Configuration de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes favoris");
        }

        dbHelper = new DatabaseHelper(this);
        setupViews();
        registerBroadcastReceiver();
        loadFavorites();
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter("com.example.recipeapp.RECIPE_UPDATED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }
    }

    private void setupViews() {
        recyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyView = findViewById(R.id.emptyView);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, new ArrayList<>(), false);
        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            List<Recipe> favorites = dbHelper.getFavorites(userId);
            adapter.updateRecipes(favorites);

            // Mettre à jour la visibilité
            if (favorites.isEmpty()) {
                emptyView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyView.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerBroadcastReceiver();
        loadFavorites();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(updateReceiver);
        } catch (IllegalArgumentException e) {
            // Ignorer si le receiver n'était pas enregistré
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
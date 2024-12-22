package com.example.recipeapp.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;
import java.util.ArrayList;
import java.util.List;

public class UserRecipesActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        @androidx.annotation.RequiresApi(api = android.os.Build.VERSION_CODES.TIRAMISU)
        public void onReceive(Context context, Intent intent) {
            if ("com.example.recipeapp.RECIPE_UPDATED".equals(intent.getAction())) {
                loadUserRecipes();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_recipes);

        // Configuration de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Mes recettes");
        }

        dbHelper = new DatabaseHelper(this);
        setupRecyclerView();
        loadUserRecipes();
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.userRecipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, new ArrayList<>(), false);
        recyclerView.setAdapter(adapter);
    }

    private void loadUserRecipes() {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);

        if (userId != -1) {
            List<Recipe> userRecipes = dbHelper.getUserRecipes(userId);
            adapter.updateRecipes(userRecipes);

            View emptyView = findViewById(R.id.emptyView);
            if (userRecipes.isEmpty()) {
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
        IntentFilter filter = new IntentFilter("com.example.recipeapp.RECIPE_UPDATED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(updateReceiver, filter);
        }
        loadUserRecipes();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(updateReceiver);
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
package com.example.recipeapp.ui;

import static android.content.Context.MODE_PRIVATE;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Recipe;
import com.example.recipeapp.models.UserPreferences;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class RecipeListFragment extends Fragment {
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private List<Recipe> allRecipes;
    private RadioGroup difficultyGroup;
    private SeekBar timeSeekBar;
    private TextView timeFilterText;
    private SearchView searchView;
    private BottomSheetDialog filterDialog;
    private View filterView;
    private FloatingActionButton shoppingListFab;

    private BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        @androidx.annotation.RequiresApi(api = android.os.Build.VERSION_CODES.TIRAMISU)
        public void onReceive(Context context, Intent intent) {
            if ("com.example.recipeapp.RECIPE_UPDATED".equals(intent.getAction())) {
                loadRecipes();
            }
        }
    };

    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("com.example.recipeapp.RECIPE_UPDATED");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requireContext().registerReceiver(updateReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
        }
        loadRecipes();
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(updateReceiver);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe_list, container, false);

        dbHelper = new DatabaseHelper(getContext());
        initViews(view);
        setupFilterDialog(inflater);
        setupListeners();
        loadRecipes();

        shoppingListFab = view.findViewById(R.id.shoppingListFab);
        shoppingListFab.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), ShoppingListActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recipeRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        Button filterButton = view.findViewById(R.id.filterButton);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new RecipeAdapter(getContext(), new ArrayList<>(), false);
        recyclerView.setAdapter(adapter);

        filterButton.setOnClickListener(v -> filterDialog.show());
    }

    private void setupFilterDialog(LayoutInflater inflater) {
        filterDialog = new BottomSheetDialog(requireContext());
        filterView = inflater.inflate(R.layout.layout_filters, null);
        filterDialog.setContentView(filterView);

        difficultyGroup = filterView.findViewById(R.id.difficultyGroup);
        timeSeekBar = filterView.findViewById(R.id.timeSeekBar);
        timeFilterText = filterView.findViewById(R.id.timeFilterText);
        Button applyButton = filterView.findViewById(R.id.applyFiltersButton);

        timeSeekBar.setProgress(180);

        applyButton.setOnClickListener(v -> {
            loadRecipes();
            filterDialog.dismiss();
        });
    }

    private void setupListeners() {
        // Listener pour la recherche
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadRecipes();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                loadRecipes();
                return true;
            }
        });

        // Listener pour la difficulté
        difficultyGroup.setOnCheckedChangeListener((group, checkedId) -> {
            loadRecipes();
        });

        // Listener pour le temps
        timeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                timeFilterText.setText("Temps maximum : " + progress + " min");
                loadRecipes();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void loadRecipes() {
        // Récupérer les préférences utilisateur
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("userId", -1);
        UserPreferences userPrefs = null;
        if (userId != -1) {
            userPrefs = dbHelper.getUserPreferences(userId);
        }

        // Récupérer les filtres actuels
        String searchQuery = searchView.getQuery().toString();
        String selectedDifficulty = null;
        int checkedId = difficultyGroup.getCheckedRadioButtonId();
        if (checkedId == R.id.filterEasy) selectedDifficulty = "Facile";
        else if (checkedId == R.id.filterMedium) selectedDifficulty = "Moyen";
        else if (checkedId == R.id.filterHard) selectedDifficulty = "Difficile";
        int maxTime = timeSeekBar.getProgress();

        // Utiliser la méthode du DatabaseHelper qui gère tous les filtres
        List<Recipe> filteredRecipes = dbHelper.getFilteredRecipes(userPrefs, searchQuery, selectedDifficulty, maxTime);
        adapter.updateRecipes(filteredRecipes);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
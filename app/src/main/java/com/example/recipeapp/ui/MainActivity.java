package com.example.recipeapp.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.example.recipeapp.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupViewPager();
        setupBottomNavigation();
    }

    private void initializeViews() {
        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
    }

    private void setupViewPager() {
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // Désactive le swipe
    }

    private void setupBottomNavigation() {
        // Sélectionner l'onglet "Aujourd'hui" par défaut au démarrage
        viewPager.setCurrentItem(0, false);
        bottomNavigation.setSelectedItemId(R.id.daily);

        bottomNavigation.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.daily) {
                viewPager.setCurrentItem(0, false);
                return true;
            } else if (itemId == R.id.recipes) {
                viewPager.setCurrentItem(1, false);
                return true;
            } else if (itemId == R.id.community) {
                viewPager.setCurrentItem(2, false);
                return true;
            } else if (itemId == R.id.suggestions) {
                viewPager.setCurrentItem(3, false);
                return true;
            } else if (itemId == R.id.account) {
                viewPager.setCurrentItem(4, false);
                return true;
            }
            return false;
        });

        // Désactiver la précharge des fragments
        viewPager.setOffscreenPageLimit(1);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.daily);
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.recipes);
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.community);
                        break;
                    case 3:
                        bottomNavigation.setSelectedItemId(R.id.suggestions);
                        break;
                    case 4:
                        bottomNavigation.setSelectedItemId(R.id.account);
                        break;
                }
            }
        });
    }

    private static class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 0:
                    return new DailyRecipeFragment();
                case 1:
                    return new RecipeListFragment();
                case 2:
                    return new CommunityRecipesFragment();
                case 3:
                    return new SuggestionsFragment();
                case 4:
                    return new AccountFragment();
                default:
                    return new DailyRecipeFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 5;
        }
    }
}
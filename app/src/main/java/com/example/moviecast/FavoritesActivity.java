package com.example.moviecast;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.moviecast.databinding.ActivityFavoritesBinding;
import com.example.moviecast.ui.adapter.FavoritesAdapter;
import com.example.moviecast.ui.utils.ThemeManager;
import com.example.moviecast.ui.viewmodel.FavoritesViewModel;

/**
 * FavoritesActivity - экран списка избранных фильмов.
 * View в MVVM: данные из Room через FavoritesViewModel.
 */
public class FavoritesActivity extends AppCompatActivity {

    private ActivityFavoritesBinding binding;
    private FavoritesViewModel viewModel;
    private FavoritesAdapter adapter;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Избранное");
        }

        setupRecyclerView();
        setupViewModel();
        setupBottomNavigation();
    }

    // Сетка 2 колонки, по клику переход на экран деталей
    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        binding.recyclerView.setLayoutManager(layoutManager);
        
        adapter = new FavoritesAdapter(mediaItem -> {
            Intent intent = new Intent(FavoritesActivity.this, MovieDetailsActivity.class);
            intent.putExtra("movie_id", mediaItem.getId());
            startActivity(intent);
        });
        
        binding.recyclerView.setAdapter(adapter);
    }

    // Подписка на список избранного из Room
    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(FavoritesViewModel.class);
        
        viewModel.getFavorites().observe(this, favorites -> {
            if (favorites != null) {
                adapter.setFavorites(favorites);
                
                // Пустой список - показываем заглушку
                if (favorites.isEmpty()) {
                    binding.emptyTextView.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyTextView.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_movies) {
                finish();
                return true;
            } else if (itemId == R.id.nav_favorites) {
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.bottomNavigation.setSelectedItemId(R.id.nav_favorites);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;
        updateThemeIcon();
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_theme_toggle) {
            ThemeManager.toggleTheme(this);
            updateThemeIcon();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void updateThemeIcon() {
        if (menu != null) {
            MenuItem themeItem = menu.findItem(R.id.action_theme_toggle);
            if (themeItem != null) {
                if (ThemeManager.isDarkMode(this)) {
                    themeItem.setIcon(R.drawable.ic_light_mode);
                    themeItem.setTitle("Переключить на светлую тему");
                } else {
                    themeItem.setIcon(R.drawable.ic_dark_mode);
                    themeItem.setTitle("Переключить на тёмную тему");
                }
            }
        }
    }
}

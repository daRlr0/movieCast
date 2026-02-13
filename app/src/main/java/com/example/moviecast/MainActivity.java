package com.example.moviecast;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.moviecast.databinding.ActivityMainBinding;
import com.example.moviecast.ui.adapter.MovieAdapter;
import com.example.moviecast.ui.utils.PaginationScrollListener;
import com.example.moviecast.ui.utils.ThemeManager;
import com.example.moviecast.ui.viewmodel.MainViewModel;
import com.google.android.material.chip.Chip;

/**
 * MainActivity - главный экран: сетка фильмов, поиск, фильтры, избранное.
 * View в MVVM, данные из MainViewModel через LiveData.
 */
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private MovieAdapter adapter;
    private GridLayoutManager layoutManager;
    private boolean isLoadingMore = false;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        
        // Инициализация ViewBinding для доступа к элементам UI
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Media Explorer");
        }

        // Инициализация всех компонентов интерфейса
        setupRecyclerView();      // Настройка сетки фильмов
        setupViewModel();         // Настройка ViewModel и подписка на данные
        setupSearchView();        // Настройка поиска
        setupFilters();           // Настройка фильтров (Chips)
        setupBottomNavigation();  // Настройка нижней навигации
    }
    
    /**
     * Создание меню в Toolbar
     * Загружает меню с кнопкой переключения темы
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Загружаем меню из XML ресурса
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu = menu;
        // Обновляем иконку темы (солнце/луна) в зависимости от текущей темы
        updateThemeIcon();
        return true;
    }
    
    /**
     * Обработка нажатий на элементы меню
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Проверяем, что нажата кнопка переключения темы
        if (item.getItemId() == R.id.action_theme_toggle) {
            // Переключаем тему через ThemeManager (использует SharedPreferences)
            ThemeManager.toggleTheme(this);
            // Обновляем иконку после переключения
            updateThemeIcon();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    /**
     * Обновление иконки переключения темы
     * Показывает солнце в темной теме (переключение на светлую)
     * Показывает луну в светлой теме (переключение на темную)
     */
    private void updateThemeIcon() {
        if (menu != null) {
            MenuItem themeItem = menu.findItem(R.id.action_theme_toggle);
            if (themeItem != null) {
                if (ThemeManager.isDarkMode(this)) {
                    // Сейчас темная тема - показываем иконку солнца (переключение на светлую)
                    themeItem.setIcon(R.drawable.ic_light_mode);
                    themeItem.setTitle("Switch to Light Mode");
                } else {
                    // Сейчас светлая тема - показываем иконку луны (переключение на темную)
                    themeItem.setIcon(R.drawable.ic_dark_mode);
                    themeItem.setTitle("Switch to Dark Mode");
                }
            }
        }
    }

    /**
     * Настройка RecyclerView для отображения сетки фильмов
     * Инициализация адаптера, LayoutManager и слушателя прокрутки для пагинации
     */
    private void setupRecyclerView() {
        // Создаем GridLayoutManager для сетки из 2 колонок
        layoutManager = new GridLayoutManager(this, 2);
        binding.recyclerView.setLayoutManager(layoutManager);
        
        // Создаем адаптер с двумя слушателями (клик по фильму и клик по кнопке избранного)
        adapter = new MovieAdapter(
                // Слушатель клика по карточке фильма
                (movie, posterView) -> {
                    // Создаем Intent для перехода на экран деталей
                    Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
                    intent.putExtra("movie_id", movie.getId());
                    
                    // Добавляем анимацию shared element transition для плавного перехода постера
                    if (posterView != null && ViewCompat.getTransitionName(posterView) != null) {
                        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                                this,
                                posterView,
                                ViewCompat.getTransitionName(posterView)
                        );
                        startActivity(intent, options.toBundle());
                    } else {
                        // Обычный переход без анимации
                        startActivity(intent);
                    }
                },
                // Слушатель клика по кнопке избранного (сердечко)
                (movieId, currentStatus) -> {
                    // Переключаем статус избранного через ViewModel (Room операция)
                    viewModel.toggleFavorite(movieId);
                }
        );
        
        // Устанавливаем адаптер в RecyclerView
        binding.recyclerView.setAdapter(adapter);
        
        // Добавляем слушатель прокрутки для бесконечной пагинации
        binding.recyclerView.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                // Пользователь доскроллил до конца - загружаем следующую страницу
                isLoadingMore = true;
                viewModel.loadNextPage(); // API запрос через Retrofit
            }

            @Override
            protected boolean isLoading() {
                // Возвращаем состояние загрузки
                return isLoadingMore;
            }

            @Override
            protected boolean hasMorePages() {
                // Проверяем наличие следующих страниц
                return viewModel.hasMorePages();
            }
        });
    }

    /**
     * Настройка ViewModel и подписка на изменения данных через LiveData
     * Здесь мы наблюдаем за тремя источниками данных: фильмы, состояние загрузки, ошибки
     */
    private void setupViewModel() {
        // Получаем экземпляр ViewModel (переживает поворот экрана)
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        
        // НАБЛЮДЕНИЕ ЗА ИЗМЕНЕНИЯМИ: Подписываемся на список фильмов из ViewModel
        viewModel.getMovies().observe(this, movies -> {
            if (movies != null) {
                // Обновляем адаптер новыми данными
                adapter.setMovies(movies);
                // Сбрасываем флаг загрузки после получения данных
                isLoadingMore = false;
                
                // Обработка пустого состояния (нет результатов поиска)
                if (movies.isEmpty()) {
                    // Показываем сообщение "Ничего не найдено"
                    binding.emptyStateLayout.setVisibility(View.VISIBLE);
                    binding.recyclerView.setVisibility(View.GONE);
                } else {
                    // Показываем список фильмов
                    binding.emptyStateLayout.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
        
        // НАБЛЮДЕНИЕ ЗА ИЗМЕНЕНИЯМИ: Подписываемся на состояние загрузки
        viewModel.getLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                // Показываем/скрываем ProgressBar в зависимости от состояния загрузки
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });
        
        // НАБЛЮДЕНИЕ ЗА ИЗМЕНЕНИЯМИ: Подписываемся на ошибки
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                // Показываем Toast с текстом ошибки (например, "Нет подключения к интернету")
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    /**
     * Настройка фильтров (Chips) для фильтрации фильмов по жанрам и годам
     * Фильтрация выполняется на клиентской стороне без запросов к API
     */
    private void setupFilters() {
        // Chip "Все" - сброс всех фильтров
        binding.chipAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.clearFilters(); // Показать все загруженные фильмы
            }
        });
        
        // Chip "Боевик" - фильтр по жанру (ID 28 = Action)
        binding.chipAction.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.setGenreFilter(28); // Фильтрация по жанру "Боевик"
            }
        });
        
        // Chip "Комедия" - фильтр по жанру (ID 35 = Comedy)
        binding.chipComedy.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.setGenreFilter(35); // Фильтрация по жанру "Комедия"
            }
        });
        
        // Chip "Драма" - фильтр по жанру (ID 18 = Drama)
        binding.chipDrama.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.setGenreFilter(18); // Фильтрация по жанру "Драма"
            }
        });
        
        // Chip "2024" - фильтр по году выпуска
        binding.chipYear2024.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.setYearFilter(2024); // Фильтрация по 2024 году
            }
        });
        
        // Chip "2023" - фильтр по году выпуска
        binding.chipYear2023.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                viewModel.setYearFilter(2023); // Фильтрация по 2023 году
            }
        });
    }

    /**
     * Настройка поиска фильмов через SearchView
     * API запрос к TMDb при подтверждении поиска
     */
    private void setupSearchView() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Пользователь нажал кнопку поиска - выполняем API запрос
                viewModel.searchMovies(query); // Retrofit запрос к /search/movie
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Текст поиска изменился
                if (newText.isEmpty()) {
                    // Если поле очищено - возвращаемся к популярным фильмам
                    viewModel.loadPopularMovies(); // Retrofit запрос к /movie/popular
                }
                return false;
            }
        });
    }

    /**
     * Настройка нижней навигации для переключения между экранами
     * Навигация между главным экраном (фильмы) и избранным
     */
    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_movies) {
                // Уже на экране фильмов - ничего не делаем
                return true;
            } else if (itemId == R.id.nav_favorites) {
                // Переход на экран избранного
                Intent intent = new Intent(MainActivity.this, FavoritesActivity.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    /**
     * Метод onResume - вызывается при возвращении на экран
     * Устанавливаем правильный выбранный элемент в нижней навигации
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Устанавливаем активным пункт "Фильмы" в нижней навигации
        binding.bottomNavigation.setSelectedItemId(R.id.nav_movies);
    }
}
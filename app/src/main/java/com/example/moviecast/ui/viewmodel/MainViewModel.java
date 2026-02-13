package com.example.moviecast.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moviecast.data.local.MediaItem;
import com.example.moviecast.data.model.MovieWithFavorite;
import com.example.moviecast.data.remote.model.Movie;
import com.example.moviecast.data.remote.model.MovieResponse;
import com.example.moviecast.data.repository.MovieRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call; // Retrofit - библиотека для сетевых запросов
import retrofit2.Callback; // Retrofit - обработка асинхронных ответов
import retrofit2.Response; // Retrofit - обертка для HTTP ответов

/**
 * MainViewModel - ViewModel для главного экрана приложения
 * 
 * Роль в MVVM:
 * Это ViewModel, которая управляет данными для главного экрана с сеткой фильмов.
 * ViewModel отвечает за бизнес-логику, загрузку данных из Repository и предоставление
 * данных для UI через LiveData. ViewModel переживает изменения конфигурации (поворот экрана).
 * 
 * Основные функции:
 * - Загрузка популярных фильмов через Retrofit
 * - Поиск фильмов по запросу
 * - Пагинация (подгрузка следующих страниц)
 * - Управление избранными фильмами через Room
 * - Фильтрация по жанрам и годам
 */
public class MainViewModel extends AndroidViewModel {
    
    // Repository для работы с данными (Retrofit + Room)
    private MovieRepository repository;
    
    // LiveData для наблюдения за списком фильмов с информацией об избранном
    private MutableLiveData<List<MovieWithFavorite>> moviesLiveData;
    
    // LiveData для отображения индикатора загрузки
    private MutableLiveData<Boolean> loadingLiveData;
    
    // LiveData для отображения ошибок
    private MutableLiveData<String> errorLiveData;
    
    // LiveData для уведомления об изменении статуса избранного у конкретного фильма
    private MutableLiveData<Integer> favoriteToggledLiveData;
    
    // Список всех загруженных фильмов
    private List<Movie> allMovies;
    
    // Список отфильтрованных фильмов (по жанру или году)
    private List<Movie> filteredMovies;
    
    // Карта для кэширования статуса избранного (ID фильма -> избранный?)
    private Map<Integer, Boolean> favoriteStatusMap;
    
    // Executor для выполнения операций с базой данных Room в фоновом потоке
    private Executor executor;
    
    // Текущая страница пагинации
    private int currentPage = 1;
    
    // Общее количество страниц
    private int totalPages = 1;
    
    // Флаг состояния загрузки
    private boolean isLoading = false;
    
    // Текущий поисковый запрос
    private String currentQuery = "";
    
    // Флаг режима поиска
    private boolean isSearchMode = false;
    
    // ID жанра для фильтрации (null = без фильтра)
    private Integer filterGenreId = null;
    
    // Год для фильтрации (null = без фильтра)
    private Integer filterYear = null;
    
    /**
     * Конструктор ViewModel
     * Инициализирует Repository, LiveData и запускает загрузку популярных фильмов
     */
    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
        moviesLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        favoriteToggledLiveData = new MutableLiveData<>();
        favoriteStatusMap = new HashMap<>();
        executor = Executors.newSingleThreadExecutor();
        allMovies = new ArrayList<>();
        // Загружаем популярные фильмы при создании ViewModel
        loadPopularMovies();
    }
    
    /**
     * Получить LiveData со списком фильмов
     * Activity/Fragment подписывается на изменения этих данных
     */
    public LiveData<List<MovieWithFavorite>> getMovies() {
        return moviesLiveData;
    }
    
    /**
     * Получить LiveData для уведомления об изменении избранного
     * Возвращает ID фильма, статус которого изменился
     */
    public LiveData<Integer> getFavoriteToggled() {
        return favoriteToggledLiveData;
    }
    
    /**
     * Получить LiveData для отслеживания состояния загрузки
     */
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }
    
    /**
     * Получить LiveData для отображения ошибок
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    /**
     * Метод для загрузки популярных фильмов из API (первая страница)
     * Использует Retrofit для выполнения сетевого запроса
     * Очищает предыдущие данные и начинает загрузку заново
     */
    public void loadPopularMovies() {
        if (isLoading) return;
        
        isSearchMode = false;
        currentPage = 1;
        allMovies.clear();
        favoriteStatusMap.clear();
        isLoading = true;
        loadingLiveData.setValue(true);
        
        // Retrofit - выполнение асинхронного GET запроса к TMDb API
        repository.getPopularMovies(currentPage).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                isLoading = false;
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    totalPages = movieResponse.getTotalPages();
                    allMovies.addAll(movieResponse.getResults());
                    // Загружаем статусы избранного из Room и обновляем UI
                    loadFavoriteStatusAndUpdate();
                } else {
                    errorLiveData.setValue("Ошибка загрузки фильмов");
                }
            }
            
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                isLoading = false;
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Нет подключения к интернету");
            }
        });
    }
    
    /**
     * Метод для загрузки следующей страницы фильмов (пагинация)
     * Поддерживает как обычный режим (популярные фильмы), так и режим поиска
     * Использует Retrofit для сетевого запроса
     */
    public void loadNextPage() {
        if (isLoading || currentPage >= totalPages) return;
        
        currentPage++;
        isLoading = true;
        loadingLiveData.setValue(true);
        
        Call<MovieResponse> call;
        if (isSearchMode) {
            call = repository.searchMovies(currentQuery, currentPage);
        } else {
            call = repository.getPopularMovies(currentPage);
        }
        
        // Retrofit - выполнение запроса к API
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                isLoading = false;
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    allMovies.addAll(movieResponse.getResults());
                    // Обновляем статусы избранного из Room
                    loadFavoriteStatusAndUpdate();
                } else {
                    errorLiveData.setValue("Ошибка загрузки следующей страницы");
                }
            }
            
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                isLoading = false;
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Нет подключения к интернету");
            }
        });
    }
    
    /**
     * Метод для поиска фильмов по запросу пользователя
     * Использует Retrofit для выполнения запроса к API поиска TMDb
     * Если запрос пустой, возвращается к загрузке популярных фильмов
     * 
     * @param query - поисковый запрос пользователя
     */
    public void searchMovies(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadPopularMovies();
            return;
        }
        
        if (isLoading) return;
        
        isSearchMode = true;
        currentQuery = query;
        currentPage = 1;
        allMovies.clear();
        favoriteStatusMap.clear();
        isLoading = true;
        loadingLiveData.setValue(true);
        
        // Retrofit - выполнение запроса поиска к TMDb API
        repository.searchMovies(query, currentPage).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                isLoading = false;
                loadingLiveData.setValue(false);
                
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    totalPages = movieResponse.getTotalPages();
                    allMovies.addAll(movieResponse.getResults());
                    loadFavoriteStatusAndUpdate();
                } else {
                    errorLiveData.setValue("Ошибка поиска");
                }
            }
            
            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                isLoading = false;
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Нет подключения к интернету");
            }
        });
    }
    
    /**
     * Загрузка статусов избранного из базы данных Room для всех фильмов
     * Выполняется в фоновом потоке через Executor
     * После загрузки обновляет LiveData для UI
     */
    private void loadFavoriteStatusAndUpdate() {
        executor.execute(() -> {
            // Проходим по всем фильмам и проверяем их наличие в базе Room
            for (Movie movie : allMovies) {
                // Room - READ операция: получение записи из базы данных
                MediaItem mediaItem = repository.getMediaItemByIdSync(movie.getId());
                favoriteStatusMap.put(movie.getId(), mediaItem != null && mediaItem.isFavorite());
            }
            updateMoviesWithFavoriteStatus();
        });
    }
    
    /**
     * Обновление списка фильмов с актуальными статусами избранного
     * Создает объекты MovieWithFavorite и отправляет в LiveData
     */
    private void updateMoviesWithFavoriteStatus() {
        List<MovieWithFavorite> moviesWithFavorite = new ArrayList<>();
        for (Movie movie : allMovies) {
            boolean isFavorite = favoriteStatusMap.getOrDefault(movie.getId(), false);
            moviesWithFavorite.add(new MovieWithFavorite(movie, isFavorite));
        }
        moviesLiveData.postValue(moviesWithFavorite);
    }
    
    /**
     * Переключение статуса избранного для фильма (добавить/удалить из избранного)
     * CRUD операции с Room: CREATE (insert), UPDATE (updateFavoriteStatus), DELETE (delete)
     * Выполняется в фоновом потоке
     * 
     * @param movieId - ID фильма для переключения статуса
     */
    public void toggleFavorite(int movieId) {
        executor.execute(() -> {
            // Room - READ: проверяем, существует ли запись в базе данных
            MediaItem existingItem = repository.getMediaItemByIdSync(movieId);
            
            // Находим фильм в загруженном списке
            Movie targetMovie = null;
            for (Movie movie : allMovies) {
                if (movie.getId() == movieId) {
                    targetMovie = movie;
                    break;
                }
            }
            
            if (targetMovie == null) return;
            
            boolean newFavoriteStatus;
            if (existingItem != null) {
                // Фильм уже есть в базе - переключаем статус
                newFavoriteStatus = !existingItem.isFavorite();
                if (newFavoriteStatus) {
                    // Room - UPDATE: обновляем статус избранного на true
                    repository.updateFavoriteStatus(movieId, true);
                } else {
                    // Room - DELETE: удаляем из избранного
                    repository.deleteMediaItemById(movieId);
                }
            } else {
                // Room - CREATE: создаем новую запись в избранном
                MediaItem newItem = repository.convertMovieToMediaItem(targetMovie, true, "");
                repository.insertMediaItem(newItem);
                newFavoriteStatus = true;
            }
            
            // Обновляем кэш статусов
            favoriteStatusMap.put(movieId, newFavoriteStatus);
            
            // Обновляем LiveData для UI
            updateMoviesWithFavoriteStatus();
            
            // Уведомляем, что статус конкретного фильма изменился
            favoriteToggledLiveData.postValue(movieId);
        });
    }
    
    /**
     * Проверка наличия следующих страниц для пагинации
     * 
     * @return true если есть еще страницы для загрузки
     */
    public boolean hasMorePages() {
        return currentPage < totalPages;
    }
    
    /**
     * Установка фильтра по жанру
     * После установки автоматически применяет фильтры
     * 
     * @param genreId - ID жанра для фильтрации (28=Боевик, 35=Комедия, 18=Драма)
     */
    public void setGenreFilter(Integer genreId) {
        this.filterGenreId = genreId;
        applyFilters();
    }
    
    /**
     * Установка фильтра по году выпуска
     * После установки автоматически применяет фильтры
     * 
     * @param year - год для фильтрации (например, 2024)
     */
    public void setYearFilter(Integer year) {
        this.filterYear = year;
        applyFilters();
    }
    
    /**
     * Очистка всех фильтров
     * Показывает все загруженные фильмы
     */
    public void clearFilters() {
        this.filterGenreId = null;
        this.filterYear = null;
        applyFilters();
    }
    
    /**
     * Применение активных фильтров к списку фильмов
     * Фильтрация выполняется на клиентской стороне (без запросов к API)
     * Работает в фоновом потоке
     */
    private void applyFilters() {
        executor.execute(() -> {
            if (filterGenreId == null && filterYear == null) {
                // Нет активных фильтров - показываем все фильмы
                loadFavoriteStatusAndUpdate();
                return;
            }
            
            filteredMovies = new ArrayList<>();
            for (Movie movie : allMovies) {
                boolean matchesFilter = true;
                
                // Фильтр по жанру
                if (filterGenreId != null) {
                    if (movie.getGenreIds() == null || !movie.getGenreIds().contains(filterGenreId)) {
                        matchesFilter = false;
                    }
                }
                
                // Фильтр по году
                if (filterYear != null && matchesFilter) {
                    if (movie.getReleaseDate() == null || !movie.getReleaseDate().startsWith(String.valueOf(filterYear))) {
                        matchesFilter = false;
                    }
                }
                
                // Если фильм прошел все фильтры, добавляем его
                if (matchesFilter) {
                    filteredMovies.add(movie);
                }
            }
            
            // Обновляем LiveData отфильтрованными фильмами
            List<MovieWithFavorite> moviesWithFavorite = new ArrayList<>();
            for (Movie movie : filteredMovies) {
                boolean isFavorite = favoriteStatusMap.getOrDefault(movie.getId(), false);
                moviesWithFavorite.add(new MovieWithFavorite(movie, isFavorite));
            }
            moviesLiveData.postValue(moviesWithFavorite);
        });
    }
}

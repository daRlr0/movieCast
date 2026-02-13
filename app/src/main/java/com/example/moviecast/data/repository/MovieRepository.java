package com.example.moviecast.data.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.moviecast.BuildConfig;
import com.example.moviecast.data.local.GenreDao; // Room - DAO для работы с жанрами
import com.example.moviecast.data.local.GenreEntity; // Room - Entity для жанров
import com.example.moviecast.data.local.MediaItem; // Room - Entity для фильмов
import com.example.moviecast.data.local.MovieDao; // Room - DAO для работы с фильмами
import com.example.moviecast.data.local.MovieDatabase; // Room - база данных
import com.example.moviecast.data.remote.RetrofitClient; // Retrofit - клиент для создания API сервиса
import com.example.moviecast.data.remote.TMDbApiService; // Retrofit - интерфейс API
import com.example.moviecast.data.remote.model.Cast;
import com.example.moviecast.data.remote.model.CreditsResponse;
import com.example.moviecast.data.remote.model.Genre;
import com.example.moviecast.data.remote.model.GenreResponse;
import com.example.moviecast.data.remote.model.Movie;
import com.example.moviecast.data.remote.model.MovieResponse;
import com.example.moviecast.data.remote.model.VideosResponse;
import com.google.gson.Gson; // GSON - для сериализации/десериализации JSON
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call; // Retrofit - класс для асинхронных запросов

/**
 * MovieRepository - Repository (Репозиторий) в архитектуре MVVM
 * 
 * Роль в MVVM:
 * Это Repository, который является единым источником данных для приложения.
 * Repository объединяет данные из разных источников:
 * 1. Удаленный источник - TMDb API через Retrofit
 * 2. Локальный источник - база данных Room для избранного и кэширования
 * 
 * ViewModel взаимодействует только с Repository, не зная о деталях источников данных.
 * 
 * Основные функции:
 * - Выполнение сетевых запросов к TMDb API через Retrofit
 * - Операции с локальной базой данных Room (CRUD)
 * - Конвертация данных между форматами API и базы данных
 * - Кэширование жанров для offline режима
 */
public class MovieRepository {
    
    // API ключ из BuildConfig (хранится в local.properties)
    private static final String API_KEY = BuildConfig.API_KEY;
    // Язык для всех запросов к API - русский
    private static final String LANGUAGE = "ru-RU";
    
    // Room - DAO для операций с фильмами в базе данных
    private MovieDao movieDao;
    // Room - DAO для операций с жанрами в базе данных
    private GenreDao genreDao;
    // Retrofit - сервис для выполнения HTTP запросов к TMDb API
    private TMDbApiService apiService;
    // Executor для выполнения операций с базой данных в фоновом потоке
    private Executor executor;
    // GSON - для конвертации объектов в JSON и обратно
    private Gson gson;
    // Карта для кэширования жанров (ID -> Название)
    private Map<Integer, String> genreMap;
    
    /**
     * Конструктор Repository
     * Инициализирует Room базу данных, Retrofit API сервис и загружает кэш жанров
     */
    public MovieRepository(Context context) {
        // Room - получение экземпляра базы данных
        MovieDatabase database = MovieDatabase.getInstance(context);
        movieDao = database.movieDao();
        genreDao = database.genreDao();
        // Retrofit - получение API сервиса
        apiService = RetrofitClient.getApiService();
        executor = Executors.newSingleThreadExecutor();
        // GSON - инициализация для работы с JSON
        gson = new Gson();
        genreMap = new HashMap<>();
        // Загружаем жанры из кэша при создании Repository
        loadGenresFromCache();
    }
    
    // ============ МЕТОДЫ ДЛЯ РАБОТЫ С API (Retrofit) ============
    
    /**
     * Retrofit: Получение популярных фильмов с TMDb API
     * Конечная точка: /movie/popular
     * 
     * @param page - номер страницы для пагинации
     * @return Call для асинхронного выполнения запроса
     */
    public Call<MovieResponse> getPopularMovies(int page) {
        return apiService.getPopularMovies(API_KEY, LANGUAGE, page);
    }
    
    /**
     * Retrofit: Поиск фильмов по запросу
     * Конечная точка: /search/movie
     * 
     * @param query - поисковый запрос
     * @param page - номер страницы
     * @return Call для асинхронного выполнения запроса
     */
    public Call<MovieResponse> searchMovies(String query, int page) {
        return apiService.searchMovies(API_KEY, LANGUAGE, query, page);
    }
    
    /**
     * Retrofit: Получение детальной информации о фильме
     * Конечная точка: /movie/{id}
     * 
     * @param movieId - ID фильма
     * @return Call для асинхронного выполнения запроса
     */
    public Call<Movie> getMovieDetails(int movieId) {
        return apiService.getMovieDetails(movieId, API_KEY, LANGUAGE);
    }
    
    /**
     * Retrofit: Получение актерского состава фильма
     * Конечная точка: /movie/{id}/credits
     * 
     * @param movieId - ID фильма
     * @return Call для асинхронного выполнения запроса
     */
    public Call<CreditsResponse> getMovieCredits(int movieId) {
        return apiService.getMovieCredits(movieId, API_KEY, LANGUAGE);
    }
    
    /**
     * Retrofit: Получение видео (трейлеров) фильма
     * Конечная точка: /movie/{id}/videos
     * 
     * @param movieId - ID фильма
     * @return Call для асинхронного выполнения запроса
     */
    public Call<VideosResponse> getMovieVideos(int movieId) {
        return apiService.getMovieVideos(movieId, API_KEY, LANGUAGE);
    }
    
    /**
     * Retrofit: Получение списка всех жанров фильмов
     * Конечная точка: /genre/movie/list
     * 
     * @return Call для асинхронного выполнения запроса
     */
    public Call<GenreResponse> getGenres() {
        return apiService.getGenres(API_KEY, LANGUAGE);
    }
    
    // ============ ОПЕРАЦИИ С ЖАНРАМИ (Room + Кэш) ============
    
    /**
     * Room - READ: Загрузка жанров из базы данных в кэш при инициализации
     * Выполняется в фоновом потоке
     */
    private void loadGenresFromCache() {
        executor.execute(() -> {
            // Room - READ: получение всех жанров из базы данных
            List<GenreEntity> genres = genreDao.getAllGenres();
            for (GenreEntity genre : genres) {
                genreMap.put(genre.getId(), genre.getName());
            }
        });
    }
    
    /**
     * Room - CREATE: Сохранение жанров в базу данных для offline доступа
     * Также обновляет кэш в памяти
     * 
     * @param genres - список жанров из API
     */
    public void cacheGenres(List<Genre> genres) {
        executor.execute(() -> {
            List<GenreEntity> entities = new ArrayList<>();
            for (Genre genre : genres) {
                entities.add(new GenreEntity(genre.getId(), genre.getName()));
                genreMap.put(genre.getId(), genre.getName());
            }
            // Room - CREATE/UPDATE: вставка жанров в базу данных
            genreDao.insertAll(entities);
        });
    }
    
    /**
     * Получение карты жанров из кэша (ID -> Название)
     */
    public Map<Integer, String> getGenreMap() {
        return genreMap;
    }
    
    /**
     * Room - READ: Получение количества жанров в базе данных (синхронно)
     */
    public int getGenreCountSync() {
        return genreDao.getGenreCount();
    }
    
    /**
     * Конвертация списка ID жанров в строку с названиями через запятую
     * Например: [28, 18] -> "Боевик, Драма"
     * 
     * @param genreIds - список ID жанров
     * @return строка с названиями жанров через запятую
     */
    public String getGenreNamesFromIds(List<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return "";
        }
        StringBuilder genreNames = new StringBuilder();
        for (int i = 0; i < genreIds.size(); i++) {
            Integer genreId = genreIds.get(i);
            String genreName = genreMap.get(genreId);
            if (genreName != null) {
                genreNames.append(genreName);
                if (i < genreIds.size() - 1) {
                    genreNames.append(", ");
                }
            }
        }
        return genreNames.toString();
    }
    
    // ============ ОПЕРАЦИИ С БАЗОЙ ДАННЫХ (Room CRUD) ============
    
    /**
     * Room - READ: Получение всех избранных фильмов
     * Возвращает LiveData для автоматического обновления UI
     * 
     * @return LiveData со списком всех избранных фильмов
     */
    public LiveData<List<MediaItem>> getAllFavorites() {
        return movieDao.getAllFavorites();
    }
    
    /**
     * Room - READ: Получение фильма по ID
     * Возвращает LiveData для автоматического обновления UI
     * 
     * @param id - ID фильма
     * @return LiveData с информацией о фильме
     */
    public LiveData<MediaItem> getMediaItemById(int id) {
        return movieDao.getMediaItemById(id);
    }
    
    /**
     * Room - CREATE: Добавление фильма в базу данных (в избранное)
     * Выполняется в фоновом потоке
     * 
     * @param mediaItem - объект фильма для сохранения
     */
    public void insertMediaItem(MediaItem mediaItem) {
        executor.execute(() -> movieDao.insert(mediaItem));
    }
    
    /**
     * Room - DELETE: Удаление фильма из базы данных (из избранного)
     * Выполняется в фоновом потоке
     * 
     * @param mediaItem - объект фильма для удаления
     */
    public void deleteMediaItem(MediaItem mediaItem) {
        executor.execute(() -> movieDao.delete(mediaItem));
    }
    
    /**
     * Room - DELETE: Удаление фильма из базы данных по ID
     * Выполняется в фоновом потоке
     * 
     * @param id - ID фильма для удаления
     */
    public void deleteMediaItemById(int id) {
        executor.execute(() -> movieDao.deleteById(id));
    }
    
    /**
     * Room - UPDATE: Обновление комментария пользователя для фильма
     * Выполняется в фоновом потоке
     * 
     * @param id - ID фильма
     * @param comment - новый текст комментария
     */
    public void updateComment(int id, String comment) {
        executor.execute(() -> movieDao.updateComment(id, comment));
    }
    
    /**
     * Room - UPDATE: Обновление статуса избранного для фильма
     * Выполняется в фоновом потоке
     * 
     * @param id - ID фильма
     * @param isFavorite - новый статус избранного (true/false)
     */
    public void updateFavoriteStatus(int id, boolean isFavorite) {
        executor.execute(() -> movieDao.updateFavoriteStatus(id, isFavorite));
    }
    
    /**
     * Room - READ: Синхронное получение фильма по ID
     * Используется для проверки наличия фильма в базе
     * ВНИМАНИЕ: Выполняется синхронно, вызывать только из фонового потока!
     * 
     * @param id - ID фильма
     * @return MediaItem или null если фильм не найден
     */
    public MediaItem getMediaItemByIdSync(int id) {
        return movieDao.getMediaItemByIdSync(id);
    }
    
    // ============ ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ КОНВЕРТАЦИИ ============
    
    /**
     * Конвертация объекта Movie (из API) в MediaItem (для Room базы данных)
     * Базовая версия без жанров и актеров
     * 
     * @param movie - объект фильма из API
     * @param isFavorite - статус избранного
     * @param userComment - комментарий пользователя
     * @return MediaItem для сохранения в Room
     */
    public MediaItem convertMovieToMediaItem(Movie movie, boolean isFavorite, String userComment) {
        return new MediaItem(
                movie.getId(),
                movie.getTitle(),
                movie.getOverview(),
                movie.getPosterPath(),
                movie.getReleaseDate(),
                movie.getVoteAverage(),
                isFavorite,
                userComment,
                null,  // genresJson будет установлен позже
                null   // castJson будет установлен позже
        );
    }
    
    /**
     * Конвертация объекта Movie в MediaItem с дополнительными данными
     * Расширенная версия с жанрами и актерами для offline режима
     * GSON используется для сериализации списков в JSON строки
     * 
     * @param movie - объект фильма из API
     * @param isFavorite - статус избранного
     * @param userComment - комментарий пользователя
     * @param genres - список жанров фильма
     * @param cast - список актеров фильма
     * @return MediaItem для сохранения в Room с offline данными
     */
    public MediaItem convertMovieToMediaItemWithExtras(Movie movie, boolean isFavorite, String userComment, 
                                                        List<Genre> genres, List<Cast> cast) {
        // GSON - сериализация списков в JSON для хранения в Room
        String genresJson = genres != null ? gson.toJson(genres) : null;
        String castJson = cast != null ? gson.toJson(cast) : null;
        
        return new MediaItem(
                movie.getId(),
                movie.getTitle(),
                movie.getOverview(),
                movie.getPosterPath(),
                movie.getReleaseDate(),
                movie.getVoteAverage(),
                isFavorite,
                userComment,
                genresJson,
                castJson
        );
    }
    
    /**
     * Десериализация JSON строки в список жанров
     * GSON используется для конвертации JSON обратно в объекты Java
     * Используется при загрузке избранных фильмов из Room для offline режима
     * 
     * @param genresJson - JSON строка с жанрами (из Room базы данных)
     * @return список объектов Genre
     */
    public List<Genre> getGenresFromJson(String genresJson) {
        if (genresJson == null || genresJson.isEmpty()) {
            return new ArrayList<>();
        }
        // GSON - десериализация JSON в список объектов
        Type listType = new TypeToken<List<Genre>>(){}.getType();
        return gson.fromJson(genresJson, listType);
    }
    
    /**
     * Десериализация JSON строки в список актеров
     * GSON используется для конвертации JSON обратно в объекты Java
     * Используется при загрузке избранных фильмов из Room для offline режима
     * 
     * @param castJson - JSON строка с актерами (из Room базы данных)
     * @return список объектов Cast
     */
    public List<Cast> getCastFromJson(String castJson) {
        if (castJson == null || castJson.isEmpty()) {
            return new ArrayList<>();
        }
        // GSON - десериализация JSON в список объектов
        Type listType = new TypeToken<List<Cast>>(){}.getType();
        return gson.fromJson(castJson, listType);
    }
}

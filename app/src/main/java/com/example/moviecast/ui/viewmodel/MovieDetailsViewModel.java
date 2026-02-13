package com.example.moviecast.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.moviecast.data.local.MediaItem;
import com.example.moviecast.data.remote.model.Cast;
import com.example.moviecast.data.remote.model.CreditsResponse;
import com.example.moviecast.data.remote.model.Genre;
import com.example.moviecast.data.remote.model.GenreResponse;
import com.example.moviecast.data.remote.model.Movie;
import com.example.moviecast.data.remote.model.Video;
import com.example.moviecast.data.remote.model.VideosResponse;
import com.example.moviecast.data.repository.MovieRepository;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import retrofit2.Call; // Retrofit - для асинхронных запросов
import retrofit2.Callback; // Retrofit - обработка ответов
import retrofit2.Response; // Retrofit - обертка HTTP ответа

/**
 * MovieDetailsViewModel - управление данными для экрана деталей
 * Загружает данные через Retrofit (API) и Room (локальная база)
 */
public class MovieDetailsViewModel extends AndroidViewModel {
    
    private MovieRepository repository;
    
    private MutableLiveData<Movie> movieLiveData;
    private MutableLiveData<List<Cast>> castLiveData;
    private MutableLiveData<String> genresLiveData;
    private MutableLiveData<String> trailerKeyLiveData;
    private MutableLiveData<Boolean> loadingLiveData;
    private MutableLiveData<String> errorLiveData;
    private MutableLiveData<Boolean> isFavoriteLiveData;
    private MutableLiveData<String> userCommentLiveData;
    
    private Executor executor;
    
    // Для сохранения в Room при добавлении в избранное
    private List<Cast> currentCast;
    private List<Genre> currentGenres;
    
    public MovieDetailsViewModel(@NonNull Application application) {
        super(application);
        
        repository = new MovieRepository(application);
        
        movieLiveData = new MutableLiveData<>();
        castLiveData = new MutableLiveData<>();
        genresLiveData = new MutableLiveData<>();
        trailerKeyLiveData = new MutableLiveData<>();
        loadingLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        isFavoriteLiveData = new MutableLiveData<>();
        userCommentLiveData = new MutableLiveData<>();
        
        executor = Executors.newSingleThreadExecutor();
        
        loadGenres();
    }
    
    public LiveData<Movie> getMovie() {
        return movieLiveData;
    }
    
    public LiveData<List<Cast>> getCast() {
        return castLiveData;
    }
    
    public LiveData<String> getGenres() {
        return genresLiveData;
    }
    
    public LiveData<String> getTrailerKey() {
        return trailerKeyLiveData;
    }
    
    public LiveData<Boolean> getLoading() {
        return loadingLiveData;
    }
    
    public LiveData<String> getError() {
        return errorLiveData;
    }
    
    public LiveData<Boolean> getIsFavorite() {
        return isFavoriteLiveData;
    }
    
    public LiveData<String> getUserComment() {
        return userCommentLiveData;
    }
    
    // Загрузка жанров из кэша или API
    private void loadGenres() {
        executor.execute(() -> {
            int genreCount = repository.getGenreCountSync(); // Room - READ
            
            if (genreCount == 0) {
                // Retrofit - загрузка жанров с API
                repository.getGenres().enqueue(new Callback<GenreResponse>() {
                    @Override
                    public void onResponse(Call<GenreResponse> call, Response<GenreResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            repository.cacheGenres(response.body().getGenres()); // Room - CREATE
                        }
                    }
                    
                    @Override
                    public void onFailure(Call<GenreResponse> call, Throwable t) {
                        // Не критично
                    }
                });
            }
        });
    }
    
    // Загрузка деталей фильма из Room и API
    public void loadMovieDetails(int movieId) {
        loadingLiveData.setValue(true);
        
        // Проверяем локальную базу (для offline и избранного)
        executor.execute(() -> {
            MediaItem mediaItem = repository.getMediaItemByIdSync(movieId); // Room - READ
            
            if (mediaItem != null) {
                // Фильм в избранном - загружаем offline данные
                isFavoriteLiveData.postValue(mediaItem.isFavorite());
                userCommentLiveData.postValue(mediaItem.getUserComment());
                
                // Жанры из кэша (GSON десериализация)
                if (mediaItem.getGenresJson() != null) {
                    currentGenres = repository.getGenresFromJson(mediaItem.getGenresJson());
                    String genreNames = getGenreNames(currentGenres);
                    genresLiveData.postValue(genreNames);
                }
                
                // Актеры из кэша (GSON десериализация)
                if (mediaItem.getCastJson() != null) {
                    currentCast = repository.getCastFromJson(mediaItem.getCastJson());
                    castLiveData.postValue(currentCast);
                }
            } else {
                isFavoriteLiveData.postValue(false);
                userCommentLiveData.postValue("");
            }
        });
        
        // Загрузка свежих данных из API
        // Retrofit - детали фильма
        repository.getMovieDetails(movieId).enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call, Response<Movie> response) {
                loadingLiveData.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    Movie movie = response.body();
                    movieLiveData.setValue(movie);
                    
                    // Set genres from movie
                    if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
                        currentGenres = movie.getGenres();
                        String genreNames = getGenreNames(movie.getGenres());
                        genresLiveData.setValue(genreNames);
                    } else if (movie.getGenreIds() != null) {
                        String genreNames = repository.getGenreNamesFromIds(movie.getGenreIds());
                        genresLiveData.setValue(genreNames);
                    }
                } else {
                    errorLiveData.setValue("Ошибка загрузки деталей фильма");
                }
            }
            
            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                loadingLiveData.setValue(false);
                errorLiveData.setValue("Нет подключения к интернету");
            }
        });
        
        // Retrofit - актерский состав
        repository.getMovieCredits(movieId).enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentCast = response.body().getCast();
                    castLiveData.setValue(currentCast);
                }
            }
            
            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) {
                // Не критично
            }
        });
        
        // Retrofit - трейлеры
        repository.getMovieVideos(movieId).enqueue(new Callback<VideosResponse>() {
            @Override
            public void onResponse(Call<VideosResponse> call, Response<VideosResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Video> videos = response.body().getResults();
                    if (videos != null && !videos.isEmpty()) {
                        // Ищем трейлер
                        for (Video video : videos) {
                            if ("Trailer".equals(video.getType()) && "YouTube".equals(video.getSite())) {
                                trailerKeyLiveData.setValue(video.getKey());
                                break;
                            }
                        }
                        // Если не нашли, берем первое видео
                        if (trailerKeyLiveData.getValue() == null && !videos.isEmpty()) {
                            trailerKeyLiveData.setValue(videos.get(0).getKey());
                        }
                    }
                }
            }
            
            @Override
            public void onFailure(Call<VideosResponse> call, Throwable t) {
                // Не критично
            }
        });
    }
    
    // Конвертация списка жанров в строку
    private String getGenreNames(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return "";
        }
        StringBuilder names = new StringBuilder();
        for (int i = 0; i < genres.size(); i++) {
            names.append(genres.get(i).getName());
            if (i < genres.size() - 1) {
                names.append(", ");
            }
        }
        return names.toString();
    }
    
    // Добавление/удаление фильма из избранного
    public void toggleFavorite(int movieId) {
        executor.execute(() -> {
            MediaItem existingItem = repository.getMediaItemByIdSync(movieId); // Room - READ
            Movie currentMovie = movieLiveData.getValue();
            
            if (currentMovie == null) return;
            
            if (existingItem != null) {
                // Фильм уже в базе
                boolean newFavoriteStatus = !existingItem.isFavorite();
                if (newFavoriteStatus) {
                    repository.updateFavoriteStatus(movieId, true); // Room - UPDATE
                    isFavoriteLiveData.postValue(true);
                } else {
                    repository.deleteMediaItemById(movieId); // Room - DELETE
                    isFavoriteLiveData.postValue(false);
                    userCommentLiveData.postValue("");
                }
            } else {
                // Создаем новую запись с жанрами и актерами (GSON сериализация)
                MediaItem newItem = repository.convertMovieToMediaItemWithExtras(
                        currentMovie, true, "", currentGenres, currentCast);
                repository.insertMediaItem(newItem); // Room - CREATE
                isFavoriteLiveData.postValue(true);
            }
        });
    }
    
    // Обновление комментария пользователя
    public void updateComment(int movieId, String comment) {
        executor.execute(() -> {
            MediaItem existingItem = repository.getMediaItemByIdSync(movieId); // Room - READ
            Movie currentMovie = movieLiveData.getValue();
            
            if (currentMovie == null) return;
            
            if (existingItem != null) {
                repository.updateComment(movieId, comment); // Room - UPDATE
                userCommentLiveData.postValue(comment);
            } else {
                // Фильма нет в базе - создаем новую запись
                MediaItem newItem = repository.convertMovieToMediaItemWithExtras(
                        currentMovie, true, comment, currentGenres, currentCast);
                repository.insertMediaItem(newItem); // Room - CREATE
                isFavoriteLiveData.postValue(true);
                userCommentLiveData.postValue(comment);
            }
        });
    }
}

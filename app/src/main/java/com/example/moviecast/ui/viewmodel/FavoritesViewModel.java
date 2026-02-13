package com.example.moviecast.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.moviecast.data.local.MediaItem;
import com.example.moviecast.data.repository.MovieRepository;

import java.util.List;

/**
 * FavoritesViewModel - данные для экрана избранного.
 * Читает список из Room через Repository.
 */
public class FavoritesViewModel extends AndroidViewModel {
    
    private MovieRepository repository;
    private LiveData<List<MediaItem>> favoritesLiveData;
    
    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        repository = new MovieRepository(application);
        favoritesLiveData = repository.getAllFavorites(); // Room - READ
    }
    
    public LiveData<List<MediaItem>> getFavorites() {
        return favoritesLiveData;
    }
    
    // Удаление из избранного (Room - DELETE)
    public void removeFromFavorites(MediaItem mediaItem) {
        repository.deleteMediaItem(mediaItem);
    }
}

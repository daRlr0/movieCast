package com.example.moviecast.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * MovieDao - доступ к таблице media_items в Room (CRUD).
 */
@Dao
public interface MovieDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaItem mediaItem);
    
    @Delete
    void delete(MediaItem mediaItem);
    
    @Query("UPDATE media_items SET userComment = :comment WHERE id = :id")
    void updateComment(int id, String comment);
    
    @Query("SELECT * FROM media_items WHERE isFavorite = 1")
    LiveData<List<MediaItem>> getAllFavorites();
    
    @Query("SELECT * FROM media_items WHERE id = :id")
    LiveData<MediaItem> getMediaItemById(int id);
    
    @Query("SELECT * FROM media_items WHERE id = :id")
    MediaItem getMediaItemByIdSync(int id);
    
    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);
    
    @Query("DELETE FROM media_items WHERE id = :id")
    void deleteById(int id);
}

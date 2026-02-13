package com.example.moviecast.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface GenreDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GenreEntity> genres);
    
    @Query("SELECT * FROM genres")
    List<GenreEntity> getAllGenres();
    
    @Query("SELECT * FROM genres WHERE id = :id")
    GenreEntity getGenreById(int id);
    
    @Query("SELECT COUNT(*) FROM genres")
    int getGenreCount();
}

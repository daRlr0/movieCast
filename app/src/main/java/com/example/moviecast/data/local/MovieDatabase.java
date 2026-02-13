package com.example.moviecast.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MediaItem.class, GenreEntity.class}, version = 2, exportSchema = false)
public abstract class MovieDatabase extends RoomDatabase {
    
    private static MovieDatabase instance;
    
    public abstract MovieDao movieDao();
    public abstract GenreDao genreDao();
    
    public static synchronized MovieDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MovieDatabase.class,
                    "movie_database"
            )
            .fallbackToDestructiveMigration()
            .build();
        }
        return instance;
    }
}

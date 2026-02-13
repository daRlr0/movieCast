package com.example.moviecast.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * GenreEntity - таблица жанров в Room (кэш с API для offline).
 */
@Entity(tableName = "genres")
public class GenreEntity {

    @PrimaryKey
    private int id;

    private String name;

    public GenreEntity(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}

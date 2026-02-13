package com.example.moviecast.data.local;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * MediaItem - сущность Room (таблица media_items).
 * Избранные фильмы + комментарий, жанры и актёры в JSON для offline.
 */
@Entity(tableName = "media_items")
public class MediaItem {
    
    @PrimaryKey
    private int id;
    
    private String title;
    private String overview;
    private String posterPath;
    private String releaseDate;
    private double voteAverage;
    private boolean isFavorite;
    private String userComment;
    
    // Жанры и актёры в JSON (GSON) для офлайн-режима
    private String genresJson;
    private String castJson;

    public MediaItem(int id, String title, String overview, String posterPath, 
                     String releaseDate, double voteAverage, boolean isFavorite, String userComment,
                     String genresJson, String castJson) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.isFavorite = isFavorite;
        this.userComment = userComment;
        this.genresJson = genresJson;
        this.castJson = castJson;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getGenresJson() {
        return genresJson;
    }

    public void setGenresJson(String genresJson) {
        this.genresJson = genresJson;
    }

    public String getCastJson() {
        return castJson;
    }

    public void setCastJson(String castJson) {
        this.castJson = castJson;
    }
}

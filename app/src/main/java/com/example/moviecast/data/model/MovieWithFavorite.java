package com.example.moviecast.data.model;

import com.example.moviecast.data.remote.model.Movie;

/**
 * MovieWithFavorite - фильм с API + флаг избранного из Room.
 * Нужен для отображения иконки сердечка на главном экране.
 */
public class MovieWithFavorite {
    private Movie movie;
    private boolean isFavorite;

    public MovieWithFavorite(Movie movie, boolean isFavorite) {
        this.movie = movie;
        this.isFavorite = isFavorite;
    }

    public Movie getMovie() { return movie; }
    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }
}

package com.example.moviecast.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Ответ API: список жанров. */
public class GenreResponse {
    
    @SerializedName("genres")
    private List<Genre> genres;

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }
}

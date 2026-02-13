package com.example.moviecast.data.remote.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/** Ответ API: список видео/трейлеров фильма. */
public class VideosResponse {
    
    @SerializedName("id")
    private int id;
    
    @SerializedName("results")
    private List<Video> results;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Video> getResults() {
        return results;
    }

    public void setResults(List<Video> results) {
        this.results = results;
    }
}

package com.example.moviecast.data.local;

import androidx.room.Entity; // Room - аннотация для Entity (таблица)
import androidx.room.PrimaryKey; // Room - аннотация для первичного ключа

/**
 * MediaItem - Entity (таблица) в Room базе данных
 * 
 * Роль в MVVM:
 * Это Entity класс, который представляет таблицу "media_items" в SQLite базе данных.
 * Room использует аннотации для автоматического создания таблицы и SQL запросов.
 * Каждый объект MediaItem соответствует одной строке в таблице.
 * 
 * Назначение:
 * Хранение информации об избранных фильмах для offline режима.
 * Содержит все необходимые данные: детали фильма, комментарии пользователя,
 * жанры и актерский состав в формате JSON для offline доступа.
 */
@Entity(tableName = "media_items") // Room - имя таблицы в базе данных
public class MediaItem {
    
    /**
     * Первичный ключ - ID фильма из TMDb API
     * Room использует это поле как уникальный идентификатор записи
     */
    @PrimaryKey
    private int id;
    
    // Название фильма
    private String title;
    
    // Описание/синопсис фильма
    private String overview;
    
    // Путь к постеру фильма (используется с Glide для загрузки изображения)
    private String posterPath;
    
    // Дата выхода фильма
    private String releaseDate;
    
    // Средний рейтинг фильма (от 0 до 10)
    private double voteAverage;
    
    // Статус избранного (true = в избранном, false = не в избранном)
    private boolean isFavorite;
    
    // Комментарий пользователя к фильму
    private String userComment;
    
    /**
     * JSON строка с жанрами фильма для offline поддержки
     * GSON сериализует список Genre в JSON, Room сохраняет как String
     */
    private String genresJson;
    
    /**
     * JSON строка с актерским составом для offline поддержки
     * GSON сериализует список Cast в JSON, Room сохраняет как String
     */
    private String castJson;

    /**
     * Конструктор для создания объекта MediaItem
     * Все параметры соответствуют полям в таблице базы данных
     */
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

    // ============ Getters and Setters ============
    // Room требует наличия getter и setter методов для всех полей
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

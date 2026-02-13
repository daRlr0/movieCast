package com.example.moviecast.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao; // Room - аннотация для Data Access Object
import androidx.room.Delete; // Room - аннотация для операции DELETE
import androidx.room.Insert; // Room - аннотация для операции INSERT
import androidx.room.OnConflictStrategy; // Room - стратегия при конфликте (REPLACE = заменить существующую запись)
import androidx.room.Query; // Room - аннотация для SQL запросов

import java.util.List;

/**
 * MovieDao - Data Access Object для работы с таблицей media_items в Room базе данных
 * 
 * Роль в MVVM:
 * Это DAO (Data Access Object), который предоставляет методы для выполнения
 * операций с базой данных Room (CRUD - Create, Read, Update, Delete).
 * Repository использует DAO для доступа к локальным данным.
 * 
 * Room автоматически генерирует реализацию этих методов на основе аннотаций.
 */
@Dao
public interface MovieDao {
    
    /**
     * Room - CREATE: Вставка фильма в базу данных (добавление в избранное)
     * REPLACE стратегия - если фильм с таким ID уже существует, заменить его
     * 
     * @param mediaItem - объект фильма для сохранения
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaItem mediaItem);
    
    /**
     * Room - DELETE: Удаление фильма из базы данных (удаление из избранного)
     * 
     * @param mediaItem - объект фильма для удаления
     */
    @Delete
    void delete(MediaItem mediaItem);
    
    /**
     * Room - UPDATE: Обновление комментария пользователя для фильма
     * SQL запрос: UPDATE media_items SET userComment = :comment WHERE id = :id
     * 
     * @param id - ID фильма
     * @param comment - новый текст комментария
     */
    @Query("UPDATE media_items SET userComment = :comment WHERE id = :id")
    void updateComment(int id, String comment);
    
    /**
     * Room - READ: Получение всех избранных фильмов
     * SQL запрос: SELECT * FROM media_items WHERE isFavorite = 1
     * Возвращает LiveData для автоматического обновления UI при изменении данных
     * 
     * @return LiveData со списком всех избранных фильмов
     */
    @Query("SELECT * FROM media_items WHERE isFavorite = 1")
    LiveData<List<MediaItem>> getAllFavorites();
    
    /**
     * Room - READ: Получение фильма по ID (асинхронно через LiveData)
     * SQL запрос: SELECT * FROM media_items WHERE id = :id
     * 
     * @param id - ID фильма
     * @return LiveData с объектом фильма
     */
    @Query("SELECT * FROM media_items WHERE id = :id")
    LiveData<MediaItem> getMediaItemById(int id);
    
    /**
     * Room - READ: Синхронное получение фильма по ID
     * SQL запрос: SELECT * FROM media_items WHERE id = :id
     * ВНИМАНИЕ: Синхронный метод, вызывать только из фонового потока!
     * 
     * @param id - ID фильма
     * @return объект MediaItem или null
     */
    @Query("SELECT * FROM media_items WHERE id = :id")
    MediaItem getMediaItemByIdSync(int id);
    
    /**
     * Room - UPDATE: Обновление статуса избранного для фильма
     * SQL запрос: UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id
     * 
     * @param id - ID фильма
     * @param isFavorite - новый статус (true/false)
     */
    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    void updateFavoriteStatus(int id, boolean isFavorite);
    
    /**
     Room - DELETE: Удаление фильма из базы данных по ID
      фильма для удаления
     */
    @Query("DELETE FROM media_items WHERE id = :id")
    void deleteById(int id);
}

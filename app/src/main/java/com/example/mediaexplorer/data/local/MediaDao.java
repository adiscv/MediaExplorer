package com.example.mediaexplorer.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.model.UserReview;

import java.util.List;

/**
 * Интерфейс Data Access Object (DAO) для операций с базой данных медиа.
 * 
 * Этот интерфейс предоставляет комплексный API для всех операций с базой данных,
 * связанных с медиа-объектами и пользовательскими отзывами. Он следует лучшим
 * практикам Room DAO с правильными аннотациями для CRUD операций, запросов
 * и поддержки LiveData для реактивных обновлений UI.
 * 
 * Ключевые обязанности:
 * - Управление избранными фильмами с полными CRUD операциями
 * - Обработка пользовательских отзывов и оценок
 * - Предоставление реактивных потоков данных через LiveData
 * - Поддержка офлайн-доступа к сохраненному контенту
 * - Обеспечение эффективных запросов с индексированными операциями
 * 
 * @author Команда Media Explorer
 * @version 1.0
 * @since 2025-02-14
 */
@Dao
public interface MediaDao {

    /**
     * Вставляет новый медиа-объект в базу данных, заменяя любой существующий объект с тем же ID.
     * 
     * @param item Медиа-объект для вставки.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MediaItem item);

    /**
     * Обновляет существующий медиа-объект в базе данных.
     * 
     * @param item Медиа-объект для обновления.
     */
    @Update
    void update(MediaItem item);

    @Delete
    void delete(MediaItem item);

    @Query("SELECT * FROM favorites ORDER BY title ASC")
    LiveData<List<MediaItem>> getAllFavorites();

    @Query("SELECT COUNT(*) FROM favorites WHERE id = :id")
    int isInFavorites(long id);

    @Query("SELECT * FROM favorites WHERE id = :id LIMIT 1")
    MediaItem getItemById(long id);

    // Методы для пользовательских отзывов
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserReview(UserReview review);

    @Query("SELECT * FROM user_reviews WHERE movieId = :movieId LIMIT 1")
    UserReview getUserReview(long movieId);

    @Query("DELETE FROM user_reviews WHERE movieId = :movieId")
    void deleteUserReview(long movieId);
}


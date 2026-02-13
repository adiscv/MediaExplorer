package com.example.mediaexplorer.repository;

import androidx.lifecycle.LiveData;

import com.example.mediaexplorer.model.Cast;
import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.model.UserReview;

import java.util.List;

/**
 * Интерфейс репозитория, определяющий контракт для операций доступа к данным.
 * 
 * Этот интерфейс следует паттерну Repository, предоставляя чистый API для доступа к данным,
 * который абстрагирует детали источников данных (сеть vs локальная база данных). Все операции
 * разработаны для выполнения в соответствующих потоках для обеспечения отзывчивости UI.
 * 
 * @author Команда Media Explorer
 * @version 1.0
 * @since 2025-02-14
 */
public interface MediaRepository {
    /**
     * Получает список популярных медиа-объектов.
     * 
     * @param page Номер страницы для пагинации.
     * @return LiveData объект, содержащий список популярных медиа-объектов.
     */
    LiveData<List<MediaItem>> getPopular(int page);

    /**
     * Ищет медиа-объекты по строке запроса.
     * 
     * @param query Строка поискового запроса.
     * @param page Номер страницы для пагинации.
     * @return LiveData объект, содержащий список результатов поиска.
     */
    LiveData<List<MediaItem>> search(String query, int page);

    /**
     * Находит фильмы по жанрам и году.
     * 
     * @param page Номер страницы для пагинации.
     * @param genres Жанры для фильтрации.
     * @param year Год для фильтрации.
     * @return LiveData объект, содержащий список найденных фильмов.
     */
    LiveData<List<MediaItem>> discoverMovies(int page, String genres, Integer year);

    /**
     * Получает детальную информацию о медиа-объекте.
     * 
     * @param id ID медиа-объекта.
     * @return LiveData объект, содержащий детальную информацию о медиа-объекте.
     */
    LiveData<MediaItem> getDetails(long id);

    /**
     * Получает актерский состав медиа-объекта.
     * 
     * @param id ID медиа-объекта.
     * @return LiveData объект, содержащий актерский состав медиа-объекта.
     */
    LiveData<List<Cast>> getCast(long id);

    /**
     * Получает список избранных медиа-объектов.
     * 
     * @return LiveData объект, содержащий список избранных медиа-объектов.
     */
    LiveData<List<MediaItem>> getFavorites();

    void addToFavorites(MediaItem item);
    void removeFromFavorites(MediaItem item);
    boolean isInFavorites(long id);
    
    // Методы для пользовательских отзывов
    void saveUserReview(UserReview review);
    UserReview getUserReview(long movieId);
    void deleteUserReview(long movieId);
    
    // Методы для офлайн-доступа
    MediaItem getFavoriteById(long movieId);
    
    LiveData<String> getLastError();
}

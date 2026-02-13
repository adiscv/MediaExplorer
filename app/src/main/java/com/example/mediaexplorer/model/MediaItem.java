package com.example.mediaexplorer.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Сущность, представляющая фильм или сериал в базе данных.
 * 
 * Этот класс является основной моделью данных для медиаконтента во всем приложении.
 * Он поддерживает как онлайн-данные из TMDB API, так и офлайн-хранение с расширенными полями
 * для полной офлайн-функциональности.
 * 
 * @author Команда Media Explorer
 * @version 1.0
 * @since 2025-02-14
 */
@Entity(tableName = "favorites")
public class MediaItem {
    @PrimaryKey
    public long id;
    
    public String title;
    public String overview;
    public String posterPath;
    public String releaseDate;
    public float voteAverage;
    
    // Дополнительные поля для офлайн-доступа
    public String backdropPath;
    public String genres; // JSON строка или разделенные запятой значения
    public String originalLanguage;
    public long offlineTimestamp; // Когда сохранено для офлайн-доступа
    
    public MediaItem() {
        this.id = 0;
        this.title = "";
        this.overview = "";
        this.posterPath = "";
        this.releaseDate = "";
        this.voteAverage = 0.0f;
        this.backdropPath = "";
        this.genres = "";
        this.originalLanguage = "";
        this.offlineTimestamp = System.currentTimeMillis();
    }
    
    public MediaItem(long id, String title, String overview, String posterPath, 
                    String releaseDate, float voteAverage) {
        this.id = id;
        this.title = title != null ? title : "";
        this.overview = overview != null ? overview : "";
        this.posterPath = posterPath != null ? posterPath : "";
        this.releaseDate = releaseDate != null ? releaseDate : "";
        this.voteAverage = voteAverage;
        this.backdropPath = "";
        this.genres = "";
        this.originalLanguage = "";
        this.offlineTimestamp = System.currentTimeMillis();
    }
    
    // Полный конструктор для офлайн-хранения
    public MediaItem(long id, String title, String overview, String posterPath, 
                    String releaseDate, float voteAverage, String backdropPath,
                    String genres, String originalLanguage) {
        this.id = id;
        this.title = title != null ? title : "";
        this.overview = overview != null ? overview : "";
        this.posterPath = posterPath != null ? posterPath : "";
        this.releaseDate = releaseDate != null ? releaseDate : "";
        this.voteAverage = voteAverage;
        this.backdropPath = backdropPath != null ? backdropPath : "";
        this.genres = genres != null ? genres : "";
        this.originalLanguage = originalLanguage != null ? originalLanguage : "";
        this.offlineTimestamp = System.currentTimeMillis();
    }
}
// ... getters and setters can be added as needed

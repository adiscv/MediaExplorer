package com.example.mediaexplorer.model;

import androidx.annotation.NonNull;

/**
 * Класс для представления жанра фильма
 * ID жанров TMDB:
 * 28 - Action
 * 12 - Adventure
 * 16 - Animation
 * 35 - Comedy
 * 80 - Crime
 * 99 - Documentary
 * 18 - Drama
 * 10751 - Family
 * 14 - Fantasy
 * 36 - History
 * 27 - Horror
 * 10402 - Music
 * 9648 - Mystery
 * 10749 - Romance
 * 878 - Science Fiction
 * 10770 - TV Movie
 * 53 - Thriller
 * 10752 - War
 * 37 - Western
 */
public class Genre {
    public int id;
    public String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Популярные жанры с их ID согласно TMDB
    public static final Genre[] POPULAR_GENRES = {
            new Genre(28, "Боевик"),
            new Genre(12, "Приключения"),
            new Genre(16, "Мультфильм"),
            new Genre(35, "Комедия"),
            new Genre(80, "Криминал"),
            new Genre(18, "Драма"),
            new Genre(10751, "Семейный"),
            new Genre(14, "Фэнтези"),
            new Genre(27, "Ужасы"),
            new Genre(9648, "Мистика"),
            new Genre(10749, "Мелодрама"),
            new Genre(878, "Научная фантастика"),
            new Genre(53, "Триллер"),
            new Genre(10752, "Военный"),
            new Genre(37, "Вестерн")
    };

    @Override
    @NonNull
    public String toString() {
        return name;
    }

    /**
     * Получает жанр по ID
     * @param id ID жанра
     * @return объект Genre или null если не найден
     */
    public static Genre getGenreById(int id) {
        for (Genre genre : POPULAR_GENRES) {
            if (genre.id == id) {
                return genre;
            }
        }
        return null;
    }

    /**
     * Получает ID жанра по названию
     * @param name название жанра
     * @return ID жанра или -1 если не найден
     */
    public static int getIdByName(String name) {
        for (Genre genre : POPULAR_GENRES) {
            if (genre.name.equalsIgnoreCase(name)) {
                return genre.id;
            }
        }
        return -1;
    }
}


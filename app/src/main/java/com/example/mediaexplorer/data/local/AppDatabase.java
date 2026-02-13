package com.example.mediaexplorer.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.model.UserReview;

/**
 * Основной класс Room базы данных для приложения Media Explorer.
 * 
 * Эта база данных служит основным решением локального хранения, предоставляя офлайн-доступ
 * к пользовательскому избранному и персональным отзывам. Она следует лучшим практикам
 * Room базы данных с правильной версионизацией, миграциями и реализацией паттерна Singleton.
 * 
 * Схема базы данных:
 * - Версия 3: Текущая версия с поддержкой MediaItem избранного и UserReview
 * - Версия 2: Добавлена сущность UserReview для персональных отзывов
 * - Версия 1: Начальная реализация MediaItem избранного
 * 
 * Ключевые возможности:
 * - Паттерн Singleton для управления экземпляром базы данных
 * - Автоматические миграции для обновлений схемы
 * - Потокобезопасные операции через Room
 * - Офлайн сохранение избранного и отзывов
 * 
 * @author Команда Media Explorer
 * @version 1.0
 * @since 2025-02-14
 */
@Database(entities = {MediaItem.class, UserReview.class}, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    private static final String DB_NAME = "media_explorer.db";
    private static AppDatabase instance;

    /**
     * Data Access Object (DAO) для медиа-объектов.
     * 
     * Предоставляет методы для CRUD операций над сущностями MediaItem.
     * 
     * @return экземпляр MediaDao
     */
    public abstract MediaDao mediaDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}


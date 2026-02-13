package com.example.mediaexplorer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaexplorer.BuildConfig;
import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.repository.MediaRepository;
import com.example.mediaexplorer.repository.MediaRepositoryImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel для главного экрана, обрабатывающий популярные фильмы, поиск и фильтрацию.
 * 
 * Этот ViewModel управляет состоянием главной сетки медиа, включая пагинацию,
 * функциональность поиска, фильтрацию по жанрам/годам и обработку офлайн-режима.
 * Он следует паттерну MVVM, предоставляя LiveData потоки для наблюдения UI и обрабатывая
 * всю бизнес-логику, связанную с управлением медиаконтентом.
 * 
 * Ключевые обязанности:
 * - Загрузка популярных фильмов с пагинацией
 * - Выполнение поисковых запросов с пагинацией результатов
 * - Применение фильтров по жанрам и годам
 * - Управление переходами между офлайн/онлайн режимами
 * - Обработка состояний загрузки и условий ошибок
 * 
 * @author Команда Media Explorer
 * @version 1.0
 * @since 2025-02-14
 */
public class MainViewModel extends AndroidViewModel {
    private static final String TAG = "MainViewModel";
    private final MediaRepository repository;
    private final MutableLiveData<List<MediaItem>> popularLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private int currentPage = 0;
    private final List<MediaItem> allPopularItems = new ArrayList<>();

    // Фильтры
    private String selectedGenres = "";
    private Integer selectedYear = null;
    private boolean isFiltering = false;
    private String currentSearchQuery = "";
    private boolean isSearching = false;

    public MainViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MediaRepositoryImpl(application.getApplicationContext());
        Log.d(TAG, "MainViewModel initialized. API Key present: " + (!BuildConfig.TMDB_API_KEY.isEmpty()));

        // Подписываемся на ошибки из репозитория чтобы показывать детальные сообщения
        repository.getLastError().observeForever(err -> {
            if (err != null && !err.isEmpty()) {
                errorLiveData.postValue(err);
            }
        });
    }

    public LiveData<List<MediaItem>> getPopular() {
        return popularLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Загружает популярные фильмы из API с поддержкой пагинации.
     * 
     * Этот метод получает фильмы из TMDB API и обновляет LiveData.
     * Реализует пагинацию, поддерживая состояние текущей страницы и накапливая результаты.
     * Пропускает выполнение если в режиме поиска для предотвращения конфликтов данных.
     * 
     * @param page Номер страницы для загрузки (индексация с 1)
     */
    public void loadPopular(int page) {
        // Don't load popular movies if we're in search mode
        if (isSearching) {
            return;
        }
        
        isLoadingLiveData.postValue(true);
        errorLiveData.postValue(null);
        currentPage = page;

        Log.d(TAG, "Loading popular movies, page: " + page);
        Log.d(TAG, "API Key: " + (BuildConfig.TMDB_API_KEY.isEmpty() ? "EMPTY!" : "Present"));

        // Get LiveData from repository
        LiveData<List<MediaItem>> repoData = repository.getPopular(page);

        // Observe the data
        repoData.observeForever(new androidx.lifecycle.Observer<List<MediaItem>>() {
            @Override
            public void onChanged(List<MediaItem> items) {
                Log.d(TAG, "Got items from repository: " + (items != null ? items.size() : "null"));

                if (items != null && !items.isEmpty()) {
                    if (page == 1) {
                        allPopularItems.clear();
                    }
                    allPopularItems.addAll(items);
                    popularLiveData.postValue(new ArrayList<>(allPopularItems));
                    errorLiveData.postValue(null);
                    Log.d(TAG, "Total items after loading: " + allPopularItems.size());
                } else {
                    Log.e(TAG, "Empty or null items received");
                    // errorLiveData будет обновляться через repository.getLastError() в случае детальной ошибки
                    if (repository.getLastError().getValue() == null) {
                        errorLiveData.postValue("No movies received from API");
                    }
                }
                isLoadingLiveData.postValue(false);
                // Удалить наблюдатель после обработки, чтобы избежать утечки памяти
                repoData.removeObserver(this);
            }
        });
    }

    public void loadNextPage() {
        if (currentPage > 0) {
            if (!currentSearchQuery.isEmpty()) {
                // We're in search mode, load next search page
                loadNextSearchPage();
            } else if (isFiltering) {
                loadFilteredMovies(currentPage + 1, selectedGenres, selectedYear);
            } else {
                loadPopular(currentPage + 1);
            }
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    /**
     * Загружает фильмы с применением фильтров по жанрам и году
     * @param page номер страницы
     * @param genreIds строка с ID жанров (например "28,35" для Action и Comedy)
     * @param year год выпуска (null если не установлен)
     */
    public void loadFilteredMovies(int page, String genreIds, Integer year) {
        isLoadingLiveData.postValue(true);
        errorLiveData.postValue(null);
        currentPage = page;
        isFiltering = true;
        selectedGenres = genreIds;
        selectedYear = year;

        Log.d(TAG, "Loading filtered movies - page: " + page + ", genres: " + genreIds + ", year: " + year);

        LiveData<List<MediaItem>> repoData = repository.discoverMovies(page, genreIds, year);

        repoData.observeForever(new androidx.lifecycle.Observer<List<MediaItem>>() {
            @Override
            public void onChanged(List<MediaItem> items) {
                Log.d(TAG, "Got filtered items from repository: " + (items != null ? items.size() : "null"));

                if (items != null && !items.isEmpty()) {
                    if (page == 1) {
                        allPopularItems.clear();
                    }
                    allPopularItems.addAll(items);
                    popularLiveData.postValue(new ArrayList<>(allPopularItems));
                    errorLiveData.postValue(null);
                    Log.d(TAG, "Total filtered items: " + allPopularItems.size());
                } else {
                    if (page == 1) {
                        Log.e(TAG, "No results found for filters");
                        errorLiveData.postValue("No movies found with selected filters");
                    }
                }
                isLoadingLiveData.postValue(false);
                // Удалить наблюдателя после обработки
                repoData.removeObserver(this);
            }
        });
    }

    /**
     * Очищает фильтры и возвращает к загрузке популярных фильмов
     */
    public void clearFilters() {
        selectedGenres = "";
        selectedYear = null;
        isFiltering = false;
        currentSearchQuery = "";
        isSearching = false;
        currentPage = 0;
        allPopularItems.clear();
        loadPopular(1);
    }

    public void refreshData() {
        selectedGenres = "";
        selectedYear = null;
        isFiltering = false;
        currentSearchQuery = "";
        isSearching = false;
        currentPage = 0;
        allPopularItems.clear();
        loadPopular(1);
    }

    public void resetState() {
        selectedGenres = "";
        selectedYear = null;
        isFiltering = false;
        currentSearchQuery = "";
        isSearching = false;
        currentPage = 0;
        allPopularItems.clear();
        // Don't load anything - let search load its own data
    }

    /**
     * Выполняет поисковый запрос фильмов с поддержкой пагинации.
     * 
     * Этот метод ищет фильмы в TMDB API по предоставленному запросу.
     * Устанавливает приложение в режим поиска и очищает предыдущие результаты.
     * Реализует правильное управление состоянием для предотвращения конфликтов 
     * с загрузкой популярных фильмов.
     * 
     * @param query Строка поискового запроса (не null, не пустая)
     */
    public void searchMovies(String query) {
        isLoadingLiveData.postValue(true);
        errorLiveData.postValue(null);
        currentPage = 1;
        allPopularItems.clear();
        currentSearchQuery = query;
        isSearching = true;

        Log.d(TAG, "Searching movies with query: " + query);

        LiveData<List<MediaItem>> repoData = repository.search(query, 1);

        repoData.observeForever(new androidx.lifecycle.Observer<List<MediaItem>>() {
            @Override
            public void onChanged(List<MediaItem> items) {
                Log.d(TAG, "Got search results: " + (items != null ? items.size() : "null"));

                if (items != null && !items.isEmpty()) {
                    allPopularItems.addAll(items);
                    popularLiveData.postValue(new ArrayList<>(allPopularItems));
                    errorLiveData.postValue(null);
                    Log.d(TAG, "Search results loaded: " + items.size() + " items");
                } else {
                    Log.e(TAG, "No search results found");
                    errorLiveData.postValue("Фильмы не найдены");
                    popularLiveData.postValue(new ArrayList<>()); // Clear results
                }
                isLoadingLiveData.postValue(false);
                // Удалить наблюдатель после обработки
                repoData.removeObserver(this);
            }
        });
    }

    public void loadNextSearchPage() {
        if (!currentSearchQuery.isEmpty() && currentPage > 0) {
            isLoadingLiveData.postValue(true);
            
            LiveData<List<MediaItem>> repoData = repository.search(currentSearchQuery, currentPage + 1);
            
            repoData.observeForever(new androidx.lifecycle.Observer<List<MediaItem>>() {
                @Override
                public void onChanged(List<MediaItem> items) {
                    if (items != null && !items.isEmpty()) {
                        allPopularItems.addAll(items);
                        popularLiveData.postValue(new ArrayList<>(allPopularItems));
                        currentPage++;
                    }
                    isLoadingLiveData.postValue(false);
                    repoData.removeObserver(this);
                }
            });
        }
    }

    /**
     * Применяет фильтр по жанрам
     * @param genreIds строка с ID жанров (например "28,35")
     */
    public void filterByGenres(String genreIds) {
        selectedGenres = genreIds;
        currentPage = 0;
        allPopularItems.clear();
        loadFilteredMovies(1, genreIds, selectedYear);
    }

    /**
     * Применяет фильтр по году выпуска
     * @param year год выпуска
     */
    public void filterByYear(Integer year) {
        selectedYear = year;
        currentPage = 0;
        allPopularItems.clear();
        loadFilteredMovies(1, selectedGenres, year);
    }

    /**
     * Применяет оба фильтра одновременно
     * @param genreIds строка с ID жанров
     * @param year год выпуска
     */
    public void applyFilters(String genreIds, Integer year) {
        selectedGenres = genreIds;
        selectedYear = year;
        currentPage = 0;
        allPopularItems.clear();
        loadFilteredMovies(1, genreIds, year);
    }

    public String getSelectedGenres() {
        return selectedGenres;
    }

    public Integer getSelectedYear() {
        return selectedYear;
    }

    public boolean isFiltering() {
        return isFiltering;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "MainViewModel cleared");
    }
}

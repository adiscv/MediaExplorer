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

public class SearchViewModel extends AndroidViewModel {
    private static final String TAG = "SearchViewModel";
    private final MediaRepository repository;
    private final MutableLiveData<List<MediaItem>> searchResultsLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private int currentPage = 0;
    private String currentQuery = "";
    private final List<MediaItem> allSearchResults = new ArrayList<>();

    public SearchViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MediaRepositoryImpl(application.getApplicationContext());

        repository.getLastError().observeForever(err -> {
            if (err != null && !err.isEmpty()) {
                errorLiveData.postValue(err);
            }
        });
    }

    public LiveData<List<MediaItem>> getSearchResults() {
        return searchResultsLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void search(String query, int page) {
        if (BuildConfig.TMDB_API_KEY == null || BuildConfig.TMDB_API_KEY.trim().isEmpty()) {
            errorLiveData.postValue("TMDB API key is missing. Add TMDB_API_KEY to local.properties");
            searchResultsLiveData.postValue(new ArrayList<>());
            isLoadingLiveData.postValue(false);
            return;
        }

        if (page == 1) {
            currentQuery = query;
            allSearchResults.clear();
        }

        if (query == null || query.trim().isEmpty()) {
            errorLiveData.postValue("Please enter a search query");
            searchResultsLiveData.postValue(new ArrayList<>());
            Log.w(TAG, "Empty query");
            return;
        }

        isLoadingLiveData.postValue(true);
        errorLiveData.postValue(null);
        currentPage = page;

        Log.d(TAG, "Searching for: " + query + ", page: " + page);

        LiveData<List<MediaItem>> searchLiveData = repository.search(query, page);
        searchLiveData.observeForever(new androidx.lifecycle.Observer<List<MediaItem>>() {
            @Override
            public void onChanged(List<MediaItem> items) {
                Log.d(TAG, "Search response received: " + (items != null ? items.size() : "null"));

                if (items != null && !items.isEmpty()) {
                    allSearchResults.addAll(items);
                    searchResultsLiveData.postValue(new ArrayList<>(allSearchResults));
                    errorLiveData.postValue(null);
                } else {
                    if (page == 1) {
                        String currentErr = errorLiveData.getValue();
                        if (currentErr == null || currentErr.trim().isEmpty()) {
                            errorLiveData.postValue("No results found");
                        }
                    }
                }
                isLoadingLiveData.postValue(false);
                // Удалить наблюдателя после обработки
                searchLiveData.removeObserver(this);
            }
        });
    }

    public void loadNextPage() {
        search(currentQuery, currentPage + 1);
    }

    public int getCurrentPage() {
        return currentPage;
    }
}

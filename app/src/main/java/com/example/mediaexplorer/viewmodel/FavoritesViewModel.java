package com.example.mediaexplorer.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.repository.MediaRepository;
import com.example.mediaexplorer.repository.MediaRepositoryImpl;

import java.util.List;

public class FavoritesViewModel extends AndroidViewModel {
    private final MediaRepository repository;
    private final LiveData<List<MediaItem>> favoritesLiveData;
    private final MutableLiveData<String> messageLiveData = new MutableLiveData<>();

    public FavoritesViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MediaRepositoryImpl(application.getApplicationContext());
        this.favoritesLiveData = repository.getFavorites();
    }

    public LiveData<List<MediaItem>> getFavorites() {
        return favoritesLiveData;
    }

    public LiveData<String> getMessage() {
        return messageLiveData;
    }

    public void removeFromFavorites(MediaItem item) {
        repository.removeFromFavorites(item);
        messageLiveData.postValue("Removed from favorites: " + item.title);
    }

    public void addToFavorites(MediaItem item) {
        repository.addToFavorites(item);
        messageLiveData.postValue("Added to favorites: " + item.title);
    }
}


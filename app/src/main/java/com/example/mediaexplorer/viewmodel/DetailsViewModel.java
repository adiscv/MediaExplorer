package com.example.mediaexplorer.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaexplorer.model.Cast;
import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.model.UserReview;
import com.example.mediaexplorer.repository.MediaRepository;
import com.example.mediaexplorer.repository.MediaRepositoryImpl;

import java.util.List;

public class DetailsViewModel extends AndroidViewModel {
    private static final String TAG = "DetailsViewModel";
    private final MediaRepository repository;
    private final MutableLiveData<MediaItem> movieDetailsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<Cast>> castListLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public DetailsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new MediaRepositoryImpl(application.getApplicationContext());
        Log.d(TAG, "DetailsViewModel initialized");
    }

    public LiveData<MediaItem> getMovieDetails() {
        return movieDetailsLiveData;
    }

    public LiveData<List<Cast>> getCastList() {
        return castListLiveData;
    }

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadMovieDetails(long movieId) {
        Log.d(TAG, "Loading movie details for ID: " + movieId);
        isLoadingLiveData.postValue(true);
        errorLiveData.postValue(null);

        // Используем switchMap для правильного управления LiveData
        LiveData<MediaItem> detailsLiveData = repository.getDetails(movieId);
        detailsLiveData.observeForever(new androidx.lifecycle.Observer<MediaItem>() {
            @Override
            public void onChanged(MediaItem movie) {
                Log.d(TAG, "Got movie details: " + (movie != null ? movie.title : "null"));
                if (movie != null) {
                    movieDetailsLiveData.postValue(movie);
                    errorLiveData.postValue(null);
                    loadCast(movieId);
                } else {
                    // Try to load from favorites if online failed
                    loadFromFavoritesFallback(movieId);
                }
                // Удалить наблюдатель после обработки
                detailsLiveData.removeObserver(this);
            }
        });
    }

    private void loadFromFavoritesFallback(long movieId) {
        new Thread(() -> {
            try {
                // Check if we have offline data in favorites
                MediaItem offlineItem = repository.getFavoriteById(movieId);
                if (offlineItem != null) {
                    Log.d(TAG, "Loaded movie from offline storage: " + offlineItem.title);
                    movieDetailsLiveData.postValue(offlineItem);
                    errorLiveData.postValue(null);
                    // Cast won't be available offline, but that's okay
                    castListLiveData.postValue(new java.util.ArrayList<>());
                    isLoadingLiveData.postValue(false);
                } else {
                    Log.d(TAG, "Movie not found in offline storage");
                    errorLiveData.postValue("Фильм недоступен офлайн. Добавьте в избранное для офлайн-доступа.");
                    isLoadingLiveData.postValue(false);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to load from favorites", e);
                errorLiveData.postValue("Failed to load movie details");
                isLoadingLiveData.postValue(false);
            }
        }).start();
    }

    public void loadCast(long movieId) {
        Log.d(TAG, "Loading cast for movie ID: " + movieId);

        LiveData<List<Cast>> castLiveData = repository.getCast(movieId);
        castLiveData.observeForever(new androidx.lifecycle.Observer<List<Cast>>() {
            @Override
            public void onChanged(List<Cast> castList) {
                Log.d(TAG, "Got cast list: " + (castList != null ? castList.size() : "null"));
                if (castList != null) {
                    castListLiveData.postValue(castList);
                } else {
                    castListLiveData.postValue(new java.util.ArrayList<>());
                }
                isLoadingLiveData.postValue(false);
                // Удалить наблюдателя после обработки
                castLiveData.removeObserver(this);
            }
        });
    }

    public void addToFavorites(MediaItem item) {
        Log.d(TAG, "Adding to favorites: " + item.title);
        repository.addToFavorites(item);
    }

    public void removeFromFavorites(MediaItem item) {
        Log.d(TAG, "Removing from favorites: " + item.title);
        repository.removeFromFavorites(item);
    }

    public boolean isInFavorites(long movieId) {
        return repository.isInFavorites(movieId);
    }

    public void saveUserReview(UserReview review) {
        Log.d(TAG, "Saving user review for movie: " + review.movieId);
        repository.saveUserReview(review);
    }

    public UserReview getUserReview(long movieId) {
        return repository.getUserReview(movieId);
    }

    public void deleteUserReview(long movieId) {
        Log.d(TAG, "Deleting user review for movie: " + movieId);
        repository.deleteUserReview(movieId);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        Log.d(TAG, "DetailsViewModel cleared");
    }
}


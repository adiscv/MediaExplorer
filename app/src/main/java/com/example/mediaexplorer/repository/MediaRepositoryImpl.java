package com.example.mediaexplorer.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.mediaexplorer.data.local.AppDatabase;
import com.example.mediaexplorer.data.local.MediaDao;
import com.example.mediaexplorer.data.remote.ApiClient;
import com.example.mediaexplorer.data.remote.ApiService;
import com.example.mediaexplorer.data.remote.dto.CastDTO;
import com.example.mediaexplorer.data.remote.dto.CreditsResponse;
import com.example.mediaexplorer.data.remote.dto.MovieDTO;
import com.example.mediaexplorer.data.remote.dto.MovieResponse;
import com.example.mediaexplorer.model.Cast;
import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.model.UserReview;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MediaRepositoryImpl implements MediaRepository {
    private static final String TAG = "MediaRepository";
    private static final String LANGUAGE_RU = "ru-RU";
    private final ApiService apiService;
    private final MediaDao mediaDao;
    private final MutableLiveData<String> lastErrorLiveData = new MutableLiveData<>();

    public MediaRepositoryImpl(Context context) {
        this.apiService = ApiClient.getApiService();
        this.mediaDao = AppDatabase.getInstance(context).mediaDao();
    }

    @Override
    public LiveData<List<MediaItem>> getPopular(int page) {
        MutableLiveData<List<MediaItem>> liveData = new MutableLiveData<>();

        Log.d(TAG, "getPopular() called with page: " + page);

        apiService.getPopular(page, LANGUAGE_RU).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                Log.d(TAG, "onResponse called: code=" + response.code() + ", successful=" + response.isSuccessful());
                Log.d(TAG, "Response URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        MovieResponse movieResponse = response.body();
                        List<MediaItem> items = new ArrayList<>();

                        if (movieResponse.results != null) {
                            Log.d(TAG, "Results count: " + movieResponse.results.size());
                            for (MovieDTO dto : movieResponse.results) {
                                items.add(dtoToMediaItem(dto));
                            }
                        } else {
                            Log.e(TAG, "Results are null!");
                        }

                        liveData.postValue(items);
                        Log.d(TAG, "Popular movies loaded: " + items.size());
                        lastErrorLiveData.postValue(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing popular response", e);
                        e.printStackTrace();
                        lastErrorLiveData.postValue("Error parsing popular response: " + e.getMessage());
                        liveData.postValue(new ArrayList<>());
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String err = "Error loading popular: code=" + response.code() + ", message=" + response.message() + ", error: " + errorBody;
                    Log.e(TAG, err);
                    lastErrorLiveData.postValue(err);
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to load popular: ").append(t.getMessage());
                try { sb.append(" | URL: ").append(call.request().url()); } catch (Exception e) { /* ignored */ }
                Log.e(TAG, sb.toString(), t);
                t.printStackTrace();
                lastErrorLiveData.postValue(sb.toString());
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    @Override
    public LiveData<List<MediaItem>> search(String query, int page) {
        MutableLiveData<List<MediaItem>> liveData = new MutableLiveData<>();

        apiService.searchMovies(query, page, LANGUAGE_RU).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                Log.d(TAG, "Search onResponse: code=" + response.code() + ", successful=" + response.isSuccessful());
                Log.d(TAG, "Search URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        MovieResponse movieResponse = response.body();
                        List<MediaItem> items = new ArrayList<>();

                        if (movieResponse.results != null) {
                            for (MovieDTO dto : movieResponse.results) {
                                items.add(dtoToMediaItem(dto));
                            }
                        }

                        liveData.postValue(items);
                        Log.d(TAG, "Search results loaded: " + items.size());
                        lastErrorLiveData.postValue(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing search response", e);
                        e.printStackTrace();
                        lastErrorLiveData.postValue("Error parsing search response: " + e.getMessage());
                        liveData.postValue(new ArrayList<>());
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String err = "Error searching: code=" + response.code() + ", message=" + response.message() + ", error: " + errorBody;
                    Log.e(TAG, err);
                    lastErrorLiveData.postValue(err);
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                StringBuilder sb = new StringBuilder();
                sb.append("Failed to search: ").append(t.getMessage());
                try { sb.append(" | URL: ").append(call.request().url()); } catch (Exception e) { /* ignored */ }
                Log.e(TAG, sb.toString(), t);
                t.printStackTrace();
                lastErrorLiveData.postValue(sb.toString());
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    @Override
    public LiveData<MediaItem> getDetails(long id) {
        MutableLiveData<MediaItem> liveData = new MutableLiveData<>();

        apiService.getMovieDetails(id, LANGUAGE_RU).enqueue(new Callback<MovieDTO>() {
            @Override
            public void onResponse(Call<MovieDTO> call, Response<MovieDTO> response) {
                Log.d(TAG, "Details onResponse: code=" + response.code() + ", successful=" + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        MovieDTO dto = response.body();
                        MediaItem item = dtoToMediaItem(dto);
                        liveData.postValue(item);
                        Log.d(TAG, "Movie details loaded: " + item.title);
                        lastErrorLiveData.postValue(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing details response", e);
                        e.printStackTrace();
                        lastErrorLiveData.postValue("Error parsing details response: " + e.getMessage());
                        liveData.postValue(null);
                    }
                } else {
                    String err = "Error loading details: " + response.code();
                    Log.e(TAG, err);
                    lastErrorLiveData.postValue(err);
                    liveData.postValue(null);
                }
            }

            @Override
            public void onFailure(Call<MovieDTO> call, Throwable t) {
                Log.e(TAG, "Failed to load details", t);
                t.printStackTrace();
                lastErrorLiveData.postValue("Failed to load details: " + t.getMessage());
                liveData.postValue(null);
            }
        });

        return liveData;
    }

    @Override
    public LiveData<List<MediaItem>> getFavorites() {
        return mediaDao.getAllFavorites();
    }

    @Override
    public void addToFavorites(MediaItem item) {
        new Thread(() -> {
            mediaDao.insert(item);
            Log.d(TAG, "Added to favorites: " + item.title);
        }).start();
    }

    @Override
    public void removeFromFavorites(MediaItem item) {
        new Thread(() -> {
            mediaDao.delete(item);
            Log.d(TAG, "Removed from favorites: " + item.title);
        }).start();
    }

    @Override
    public boolean isInFavorites(long id) {
        return mediaDao.isInFavorites(id) > 0;
    }

    @Override
    public void saveUserReview(UserReview review) {
        new Thread(() -> {
            mediaDao.insertUserReview(review);
            Log.d(TAG, "Saved user review for movie: " + review.movieId);
        }).start();
    }

    @Override
    public UserReview getUserReview(long movieId) {
        return mediaDao.getUserReview(movieId);
    }

    @Override
    public void deleteUserReview(long movieId) {
        new Thread(() -> {
            mediaDao.deleteUserReview(movieId);
            Log.d(TAG, "Deleted user review for movie: " + movieId);
        }).start();
    }

    @Override
    public MediaItem getFavoriteById(long movieId) {
        return mediaDao.getItemById(movieId);
    }

    @Override
    public LiveData<List<Cast>> getCast(long id) {
        MutableLiveData<List<Cast>> liveData = new MutableLiveData<>();

        Log.d(TAG, "getCast() called with id: " + id);

        apiService.getCredits(id, LANGUAGE_RU).enqueue(new Callback<CreditsResponse>() {
            @Override
            public void onResponse(Call<CreditsResponse> call, Response<CreditsResponse> response) {
                Log.d(TAG, "Cast onResponse: code=" + response.code() + ", successful=" + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        CreditsResponse creditsResponse = response.body();
                        List<Cast> castList = new ArrayList<>();

                        if (creditsResponse.cast != null) {
                            Log.d(TAG, "Cast count: " + creditsResponse.cast.size());
                            for (CastDTO dto : creditsResponse.cast) {
                                castList.add(new Cast(
                                    dto.id,
                                    dto.name,
                                    dto.character,
                                    dto.profilePath
                                ));
                            }
                        } else {
                            Log.e(TAG, "Cast is null!");
                        }

                        liveData.postValue(castList);
                        Log.d(TAG, "Cast loaded: " + castList.size());
                        lastErrorLiveData.postValue(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing cast response", e);
                        e.printStackTrace();
                        lastErrorLiveData.postValue("Error parsing cast response: " + e.getMessage());
                        liveData.postValue(new ArrayList<>());
                    }
                } else {
                    Log.e(TAG, "Error loading cast: " + response.code());
                    lastErrorLiveData.postValue("Error loading cast: " + response.code());
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<CreditsResponse> call, Throwable t) {
                Log.e(TAG, "Failed to load cast", t);
                t.printStackTrace();
                lastErrorLiveData.postValue("Failed to load cast: " + t.getMessage());
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    @Override
    public LiveData<List<MediaItem>> discoverMovies(int page, String genres, Integer year) {
        MutableLiveData<List<MediaItem>> liveData = new MutableLiveData<>();

        Log.d(TAG, "discoverMovies() called with page: " + page + ", genres: " + genres + ", year: " + year);

        apiService.discoverMovies(page, genres, year, "popularity.desc", LANGUAGE_RU).enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                Log.d(TAG, "Discover onResponse: code=" + response.code() + ", successful=" + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        MovieResponse movieResponse = response.body();
                        List<MediaItem> items = new ArrayList<>();

                        if (movieResponse.results != null) {
                            Log.d(TAG, "Discover results count: " + movieResponse.results.size());
                            for (MovieDTO dto : movieResponse.results) {
                                items.add(dtoToMediaItem(dto));
                            }
                        } else {
                            Log.e(TAG, "Discover results are null!");
                        }

                        liveData.postValue(items);
                        Log.d(TAG, "Discovery loaded: " + items.size());
                        lastErrorLiveData.postValue(null);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing discover response", e);
                        e.printStackTrace();
                        lastErrorLiveData.postValue("Error parsing discover response: " + e.getMessage());
                        liveData.postValue(new ArrayList<>());
                    }
                } else {
                    String errorBody = "";
                    try {
                        errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    String err = "Error loading discover: code=" + response.code() + ", error: " + errorBody;
                    Log.e(TAG, err);
                    lastErrorLiveData.postValue(err);
                    liveData.postValue(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e(TAG, "Failed to discover movies", t);
                t.printStackTrace();
                lastErrorLiveData.postValue("Failed to discover movies: " + t.getMessage());
                liveData.postValue(new ArrayList<>());
            }
        });

        return liveData;
    }

    private MediaItem dtoToMediaItem(MovieDTO dto) {
        return new MediaItem(
                dto.id,
                dto.title,
                dto.overview,
                dto.posterPath,
                dto.releaseDate,
                (float) dto.voteAverage
        );
    }

    @Override
    public LiveData<String> getLastError() {
        return lastErrorLiveData;
    }
}


package com.example.mediaexplorer.data.remote;

import com.example.mediaexplorer.data.remote.dto.CreditsResponse;
import com.example.mediaexplorer.data.remote.dto.MovieDTO;
import com.example.mediaexplorer.data.remote.dto.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("movie/popular")
    Call<MovieResponse> getPopular(
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("search/movie")
    Call<MovieResponse> searchMovies(
            @Query("query") String query,
            @Query("page") int page,
            @Query("language") String language
    );

    @GET("discover/movie")
    Call<MovieResponse> discoverMovies(
            @Query("page") int page,
            @Query("with_genres") String genres,
            @Query("primary_release_year") Integer year,
            @Query("sort_by") String sortBy,
            @Query("language") String language
    );

    @GET("movie/{movie_id}")
    Call<MovieDTO> getMovieDetails(
            @Path("movie_id") long id,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/credits")
    Call<CreditsResponse> getCredits(
            @Path("movie_id") long id,
            @Query("language") String language
    );

    @GET("movie/{movie_id}/videos")
    Call<Object> getVideos(@Path("movie_id") long id);
}

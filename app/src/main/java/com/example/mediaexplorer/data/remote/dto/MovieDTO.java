package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MovieDTO {
    public long id;
    public String title;
    public String overview;
    @SerializedName("poster_path")
    public String posterPath;
    @SerializedName("release_date")
    public String releaseDate;
    @SerializedName("vote_average")
    public double voteAverage;
}


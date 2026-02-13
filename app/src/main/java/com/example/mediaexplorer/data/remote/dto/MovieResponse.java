package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class MovieResponse {
    public int page;
    public java.util.List<MovieDTO> results;
    @SerializedName("total_pages")
    public int totalPages;
    @SerializedName("total_results")
    public int totalResults;
}


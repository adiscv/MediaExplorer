package com.example.mediaexplorer.data.remote.dto;

import com.google.gson.annotations.SerializedName;

public class CastDTO {
    public long id;
    public String name;
    public String character;
    @SerializedName("profile_path")
    public String profilePath;
}


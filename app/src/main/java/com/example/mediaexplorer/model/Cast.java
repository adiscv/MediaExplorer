package com.example.mediaexplorer.model;

public class Cast {
    public long id;
    public String name;
    public String character;
    public String profilePath;

    public Cast(long id, String name, String character, String profilePath) {
        this.id = id;
        this.name = name;
        this.character = character;
        this.profilePath = profilePath;
    }

    // ... getters/setters
}


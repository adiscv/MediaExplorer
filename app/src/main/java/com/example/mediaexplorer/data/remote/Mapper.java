package com.example.mediaexplorer.data.remote;

import com.example.mediaexplorer.model.MediaItem;
import com.example.mediaexplorer.model.Cast;

import java.util.ArrayList;
import java.util.List;

public class Mapper {
    // Example mapper methods (to be expanded according to DTOs)
    public static MediaItem toMediaItem(com.google.gson.JsonObject json) {
        long id = json.has("id") ? json.get("id").getAsLong() : 0L;
        String title = json.has("title") ? json.get("title").getAsString() : json.has("name") ? json.get("name").getAsString() : "";
        String overview = json.has("overview") ? json.get("overview").getAsString() : "";
        String posterPath = json.has("poster_path") && !json.get("poster_path").isJsonNull() ? json.get("poster_path").getAsString() : null;
        String releaseDate = json.has("release_date") ? json.get("release_date").getAsString() : json.has("first_air_date") ? json.get("first_air_date").getAsString() : "";
        double voteAverage = json.has("vote_average") ? json.get("vote_average").getAsDouble() : 0.0;
        return new MediaItem(id, title, overview, posterPath, releaseDate, (float) voteAverage);
    }

    public static List<Cast> toCastList(com.google.gson.JsonArray array) {
        List<Cast> list = new ArrayList<>();
        if (array == null) return list;
        for (int i = 0; i < array.size(); i++) {
            com.google.gson.JsonObject obj = array.get(i).getAsJsonObject();
            long id = obj.has("id") ? obj.get("id").getAsLong() : 0L;
            String name = obj.has("name") ? obj.get("name").getAsString() : "";
            String character = obj.has("character") ? obj.get("character").getAsString() : "";
            String profilePath = obj.has("profile_path") && !obj.get("profile_path").isJsonNull() ? obj.get("profile_path").getAsString() : null;
            list.add(new Cast(id, name, character, profilePath));
        }
        return list;
    }
}


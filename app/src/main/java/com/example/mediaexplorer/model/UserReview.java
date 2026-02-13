package com.example.mediaexplorer.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user_reviews")
public class UserReview {
    @PrimaryKey
    public long movieId;
    
    public float userRating;  // 0.0 to 5.0
    public String userComment;
    public long createdAt;
    
    public UserReview() {}
    
    public UserReview(long movieId, float userRating, String userComment) {
        this.movieId = movieId;
        this.userRating = userRating;
        this.userComment = userComment;
        this.createdAt = System.currentTimeMillis();
    }
}

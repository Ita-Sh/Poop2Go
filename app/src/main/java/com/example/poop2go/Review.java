package com.example.poop2go;

public class Review {
    private String reviewId;
    private String restroomId;
    private String uId;
    private int rating;
    private String comment;
    private long timestamp;

    public Review() {} // Required for Firebase

    public Review(String reviewId, String restroomId, String uId, int rating, String comment, long timestamp) {
        this.reviewId = reviewId;
        this.restroomId = restroomId;
        this.uId = uId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public String getReviewId() { return reviewId; }
    public String getRestroomId() { return restroomId; }
    public String getUId() { return uId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public long getTimestamp() { return timestamp; }

    public void setReviewId(String reviewId) { this.reviewId = reviewId; }
    public void setRestroomId(String restroomId) { this.restroomId = restroomId; }
    public void setUId(String uId) { this.uId = uId; }
    public void setRating(int rating) { this.rating = rating; }
    public void setComment(String comment) { this.comment = comment; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
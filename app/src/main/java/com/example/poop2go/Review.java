package com.example.poop2go;

import java.security.Timestamp;

public class Review {
    private String toiletId;
    private String uId;    //The Id of the User who wrote the review
    private int rating;    //0 to 5
    private String comment;    //The content of the review. Up to 100 words.
    private Timestamp createdAt; //The date and time the review was created


    public Review(){
        this.toiletId = "";
        this.uId = "";
        this.rating = 0;
        this.comment = "";
        this.createdAt = null;
    }    //default builder
    public Review(String uid, int rating, String comment, Timestamp createdAt){
        this.toiletId = ""; //set automatically
        this.uId = uid;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public String getToiletId(){
        return this.toiletId;
    }
    public String getUId(){
        return this.uId;
    }
    public int getRating(){
        return this.rating;
    }
    public String getComment(){
        return this.comment;
    }
    public Timestamp getCreatedAt(){
        return this.createdAt;
    }

    public void setToiletId(String toiletId){
        this.toiletId = toiletId;
    }
    public void setUId(String uId){
        this.uId = uId;
    }
    public void setRating(int rating){
        this.rating = rating;
    }
    public void setComment(String comment){
        this.comment = comment;
    }
    public void setCreatedAt(Timestamp createdAt){
        this.createdAt = createdAt;
    }

}

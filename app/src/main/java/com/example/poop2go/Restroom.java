package com.example.poop2go;

import java.util.List;
import com.google.firebase.database.PropertyName;

public class Restroom {
    private String restroomId;
    private String restroomName;
    private double longitude;
    private double latitude;
    private String address;
    private double avgRating;
    private boolean isPaid;
    private boolean isSeparated;

    public Restroom(){
        this.restroomId = "";
        this.restroomName = "";
        this.longitude = 0;
        this.latitude = 0;
        this.address = "";
        this.avgRating = -1;
        this.isPaid = false;
        this.isSeparated = false;
    }    //default builder
    public Restroom (String restroomId, String restroomName, double longitude, double latitude, String address, boolean isPaid, boolean isSeparated) {
        this.restroomId = restroomId;
        this.restroomName = restroomName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.isPaid = isPaid;
        this.isSeparated = isSeparated;
        this.avgRating = -1; //updated each time a review is added
    }

    public String getRestroomId(){
        return this.restroomId;
    }
    public String getRestroomName(){
        return this.restroomName;
    }
    public double getLongitude(){
        return this.longitude;
    }
    public double getLatitude(){
        return this.latitude;
    }
    public String getAddress(){
        return this.address;
    }
    public double getAvgRating(){
        return this.avgRating;
    }
    public boolean getIsPaid(){
        return this.isPaid;
    }
    public boolean getIsSeparated(){
        return this.isSeparated;
    }

    public void setRestroomId(String toiletId){
        this.restroomId = toiletId;
    }
    public void setRestroomName(String restroomName){ this.restroomName = restroomName; }
    public void setLongitude(double longitude){
        this.longitude = longitude;
    }
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public void setAddress(String address){
        this.address = address;
    }
    public void setIsPaid(boolean isPaid){
        this.isPaid = isPaid;
    }
    public void setIsSeparated(boolean isSeparated){
        this.isSeparated = isSeparated;
    }

    public void calcAvgRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            this.avgRating = -1;
            return;
        }

        double totalWeightedSum = 0.0;
        double totalWeight = 0.0;
        long currentTime = System.currentTimeMillis();

        for (Review review : reviews) {
            long reviewTime = review.getTimestamp();

            // Calculate age in years
            double timeDifferenceInDays = (currentTime - reviewTime) / (1000.0 * 60 * 60 * 24);
            double timeDifferenceInYears = timeDifferenceInDays / 365.24;

            // Exponential decay weight
            double weight = Math.pow(Math.E, -0.5 * timeDifferenceInYears);

            totalWeightedSum += review.getRating() * weight;
            totalWeight += weight;
        }

        if (totalWeight > 0) {
            this.avgRating = totalWeightedSum / totalWeight;
        } else {
            this.avgRating = -1;
        }
    }
// Gets all the reviews for the toilet and calculates the weighted average of them, using algorithm 3 in section 4.

}

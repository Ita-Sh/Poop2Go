package com.example.poop2go;

public class Restroom {
    private String toiletId;
    private String toiletName;
    private double longitude;
    private double latitude;
    private String address;
    private double avgRating;
    private boolean isPaid;
    private boolean isSeparated;

    public Restroom(){
        this.toiletId = "";
        this.toiletName = "";
        this.longitude = 0;
        this.latitude = 0;
        this.address = "";
        this.avgRating = -1;
        this.isPaid = false;
        this.isSeparated = false;
    }    //default builder
    public Restroom (String toiletId, String toiletName, double longitude, double latitude, String address, boolean isPaid, boolean isSeparated) {
        this.toiletId = toiletId;
        this.toiletName = toiletName;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.isPaid = isPaid;
        this.isSeparated = isSeparated;
        this.avgRating = -1; //updated each time a review is added
    }

    public String getToiletId(){
        return this.toiletId;
    }
    public String getToiletName(){
        return this.toiletName;
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

    public void setToiletId(String toiletId){
        this.toiletId = toiletId;
    }
    public void setToiletName(String toiletName){
        this.toiletName = toiletName;
    }
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

    public void calcAvgRating(Review[] reviews){
        double weight;
        double totalWeightedSum = 0.0;
        double totalWeight = 0.0;
        long currentTime = System.currentTimeMillis();
        long reviewTime;
        long timeDifferenceInDays;
        double timeDifferenceInYears;

        for (int i=0; i<reviews.length; i++){
            Review review = reviews[i];
            reviewTime = review.getCreatedAt().getTimestamp().getTime();
            timeDifferenceInDays = (currentTime - reviewTime) / (1000 * 60 * 60 * 24);
            timeDifferenceInYears = timeDifferenceInDays / 365.24;
            weight = Math.pow(Math.E, -0.5 * timeDifferenceInYears);
            totalWeightedSum += review.getRating() * weight;
            totalWeight += weight;
        }
        this.avgRating = totalWeightedSum / totalWeight;
    }
// Gets all the reviews for the toilet and calculates the weighted average of them, using algorithm 3 in section 4.

}

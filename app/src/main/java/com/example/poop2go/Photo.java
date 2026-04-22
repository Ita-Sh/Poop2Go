package com.example.poop2go;

public class Photo {
    private String photoId;
    private String restroomId;
    private String uId;
    private String downloadUrl;
    private long timestamp;

    public Photo() {} // Required for Firebase

    public Photo(String photoId, String restroomId, String uId, String downloadUrl, long timestamp) {
        this.photoId = photoId;
        this.restroomId = restroomId;
        this.uId = uId;
        this.downloadUrl = downloadUrl;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getPhotoId() { return photoId; }
    public String getRestroomId() { return restroomId; }
    public String getUId() { return uId; }
    public String getDownloadUrl() { return downloadUrl; }
    public long getTimestamp() { return timestamp; }

    public void setPhotoId(String photoId) { this.photoId = photoId; }
    public void setRestroomId(String restroomId) { this.restroomId = restroomId; }
    public void setUId(String uId) { this.uId = uId; }
    public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}

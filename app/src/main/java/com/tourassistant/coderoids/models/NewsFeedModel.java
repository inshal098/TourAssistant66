package com.tourassistant.coderoids.models;

import com.google.firebase.firestore.Blob;
import com.google.firebase.firestore.GeoPoint;

public class NewsFeedModel {
    private String title;
    private String userName;
    private String dateInMillis;
    private String description;
    private Blob newsThumbNail;
    private GeoPoint geoPoint;
    private String uploadedById;
    private String rating;
    private String hazardType;
    private String tripId;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getHazardType() {
        return hazardType;
    }

    public void setHazardType(String hazardType) {
        this.hazardType = hazardType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDateInMillis() {
        return dateInMillis;
    }

    public void setDateInMillis(String dateInMillis) {
        this.dateInMillis = dateInMillis;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Blob getNewsThumbNail() {
        return newsThumbNail;
    }

    public void setNewsThumbNail(Blob newsThumbNail) {
        this.newsThumbNail = newsThumbNail;
    }

    public GeoPoint getGeoPoint() {
        return geoPoint;
    }

    public void setGeoPoint(GeoPoint geoPoint) {
        this.geoPoint = geoPoint;
    }

    public String getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(String uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }
}

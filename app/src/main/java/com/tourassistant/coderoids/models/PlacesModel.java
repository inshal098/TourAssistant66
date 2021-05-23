package com.tourassistant.coderoids.models;

import android.graphics.Bitmap;

import com.google.firebase.firestore.Blob;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.GeoPoint;

public class PlacesModel {
    private String destinationName;
    private String destinationId;
    private String destinationRating;
    private String destinationStatus;
    private String destinationImageUrl;
    private String destinationDescription;
    private GeoPoint destinationCoordinates;
    private String destinationAddress;
    private Blob blob;
    private String tripTags;
   // private Bitmap [] bitmaps;

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getDestinationRating() {
        return destinationRating;
    }

    public void setDestinationRating(String destinationRating) {
        this.destinationRating = destinationRating;
    }

    public String getDestinationStatus() {
        return destinationStatus;
    }

    public void setDestinationStatus(String destinationStatus) {
        this.destinationStatus = destinationStatus;
    }

    public String getDestinationImageUrl() {
        return destinationImageUrl;
    }

    public void setDestinationImageUrl(String destinationImageUrl) {
        this.destinationImageUrl = destinationImageUrl;
    }

    public String getDestinationDescription() {
        return destinationDescription;
    }

    public void setDestinationDescription(String destinationDescription) {
        this.destinationDescription = destinationDescription;
    }

    public GeoPoint getDestinationCoordinates() {
        return destinationCoordinates;
    }

    public void setDestinationCoordinates(GeoPoint destinationCoordinates) {
        this.destinationCoordinates = destinationCoordinates;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public Blob getBlob() {
        return blob;
    }

    public void setBlob(Blob blob) {
        this.blob = blob;
    }

    public String getTripTags() {
        return tripTags;
    }

    public void setTripTags(String tripTags) {
        this.tripTags = tripTags;
    }

    /*   public Bitmap[] getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(Bitmap[] bitmaps) {
        this.bitmaps = bitmaps;
    }*/
}

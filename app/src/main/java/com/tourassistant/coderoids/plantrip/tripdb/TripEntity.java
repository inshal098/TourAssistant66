package com.tourassistant.coderoids.plantrip.tripdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.json.JSONArray;

import java.io.Serializable;

@Entity(tableName = "TripEntity")
public class TripEntity implements Serializable {
    public TripEntity() {
    }

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "tripTitle")
    private String tripTitle;

    @ColumnInfo(name = "tripDescription")
    private String tripDescription;

    @ColumnInfo(name = "isFavourite")
    private int isFavourite;

    @ColumnInfo(name = "startDate")
    private String startDate;

    @ColumnInfo(name = "endDate")
    private String endDate;

    @ColumnInfo(name = "friendList")
    private String friends;

    @ColumnInfo(name = "tripBudget")
    private String tripBudget;

    @ColumnInfo(name = "destination")
    private String destination;

    @ColumnInfo(name = "startingPoint")
    private String startingPoint;

    @ColumnInfo(name = "isPrivate")
    private String isPrivate;

    @ColumnInfo(name = "firebaseId")
    private String firebaseId;

    @ColumnInfo(name = "firebaseUserId")
    private String firebaseUserId;

    @ColumnInfo(name = "creatorName")
    private String creatorName;

    @ColumnInfo(name = "tripTags")
    private String tripTags;

    @ColumnInfo(name = "destinationId")
    private String destinationId;

    @ColumnInfo(name = "joinTripRequests")
    private String joinTripRequests;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTripTitle() {
        return tripTitle;
    }

    public void setTripTitle(String tripTitle) {
        this.tripTitle = tripTitle;
    }

    public String getTripDescription() {
        return tripDescription;
    }

    public void setTripDescription(String tripDescription) {
        this.tripDescription = tripDescription;
    }

    public int getIsFavourite() {
        return isFavourite;
    }

    public void setIsFavourite(int isFavourite) {
        this.isFavourite = isFavourite;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getFriends() {
        return friends;
    }

    public void setFriends(String friends) {
        this.friends = friends;
    }

    public String getTripBudget() {
        return tripBudget;
    }

    public void setTripBudget(String tripBudget) {
        this.tripBudget = tripBudget;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(String startingPoint) {
        this.startingPoint = startingPoint;
    }

    public String getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(String isPrivate) {
        this.isPrivate = isPrivate;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getFirebaseUserId() {
        return firebaseUserId;
    }

    public void setFirebaseUserId(String firebaseUserId) {
        this.firebaseUserId = firebaseUserId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public String getTripTags() {
        return tripTags;
    }

    public void setTripTags(String tripTags) {
        this.tripTags = tripTags;
    }

    public String getDestinationId() {
        return destinationId;
    }

    public void setDestinationId(String destinationId) {
        this.destinationId = destinationId;
    }

    public String getJoinTripRequests() {
        return joinTripRequests;
    }

    public void setJoinTripRequests(String joinTripRequests) {
        this.joinTripRequests = joinTripRequests;
    }
}

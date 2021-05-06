package com.tourassistant.coderoids.models;

public class FriendRequestModel {
    private String userName;
    private String userFirestoreIdSender;
    private String userEmail;
    private String friendRequestId;
    private String userFirestoreIdReceiver;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserFirestoreIdSender() {
        return userFirestoreIdSender;
    }

    public void setUserFirestoreIdSender(String userFirestoreIdSender) {
        this.userFirestoreIdSender = userFirestoreIdSender;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getFriendRequestId() {
        return friendRequestId;
    }

    public void setFriendRequestId(String friendRequestId) {
        this.friendRequestId = friendRequestId;
    }

    public String getUserFirestoreIdReceiver() {
        return userFirestoreIdReceiver;
    }

    public void setUserFirestoreIdReceiver(String userFirestoreIdReceiver) {
        this.userFirestoreIdReceiver = userFirestoreIdReceiver;
    }
}

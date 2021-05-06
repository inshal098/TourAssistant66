package com.tourassistant.coderoids.models;

public class NotificationPublish {
    String notificationType;
    String notificationTime;
    String notificationMessage;
    String notificatioSender;
    String notificationReciever;
    String notificationStatus;

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getNotificationTime() {
        return notificationTime;
    }

    public void setNotificationTime(String notificationTime) {
        this.notificationTime = notificationTime;
    }

    public String getNotificationMessage() {
        return notificationMessage;
    }

    public void setNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
    }

    public String getNotificatioSender() {
        return notificatioSender;
    }

    public void setNotificatioSender(String notificatioSender) {
        this.notificatioSender = notificatioSender;
    }

    public String getNotificationReciever() {
        return notificationReciever;
    }

    public void setNotificationReciever(String notificationReciever) {
        this.notificationReciever = notificationReciever;
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String notificationStatus) {
        this.notificationStatus = notificationStatus;
    }
}

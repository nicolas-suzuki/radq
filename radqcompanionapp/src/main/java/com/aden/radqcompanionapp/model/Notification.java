package com.aden.radqcompanionapp.model;

@SuppressWarnings("ALL")
public class Notification {

    private String userId;
    private String notification;
    private String timestamp;

    public Notification() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

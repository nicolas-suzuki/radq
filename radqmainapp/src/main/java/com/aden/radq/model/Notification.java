package com.aden.radq.model;

@SuppressWarnings("ALL")
public class Notification {

    private String notification;
    private String timestamp;
    private String userId;

    public Notification() {
    }

    public final String getUserId() {
        return userId;
    }

    public final void setUserId(String userId) {
        this.userId = userId;
    }

    public final String getNotification() {
        return notification;
    }

    public final void setNotification(String notification) {
        this.notification = notification;
    }

    public final String getTimestamp() {
        return timestamp;
    }

    public final void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}

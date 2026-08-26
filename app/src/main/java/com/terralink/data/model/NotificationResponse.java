package com.terralink.data.model;

public class NotificationResponse {
    private long id;
    private String title;
    private String message;
    private String createdAt;
    private boolean isRead;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }
}

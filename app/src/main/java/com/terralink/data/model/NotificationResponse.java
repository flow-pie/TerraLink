package com.terralink.data.model;

public class NotificationResponse {
    private long id;
    private String title;
    private String body;
    private String type;
    private String createdAt;
    private boolean isRead;

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getType() {
        return type;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return isRead;
    }
}

package com.library.dto;

import java.time.LocalDateTime;

public class Activity {
    private String title;
    private String description;
    private LocalDateTime timestamp;
    private String type;
    private String icon;

    public Activity() {
    }

    public Activity(String title, String description, LocalDateTime timestamp, String type, String icon) {
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
        this.type = type;
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
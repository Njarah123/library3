package com.library.dto;

import java.util.List;
import java.util.ArrayList;

public class NotificationResponse {
    private boolean success;
    private List<NotificationDTO> notifications;
    private long unreadCount;
    private long timestamp;
    private String message;

    // Constructeur par défaut
    public NotificationResponse() {
        this.notifications = new ArrayList<>();
    }

    // Constructeur pour succès
    public NotificationResponse(boolean success, List<NotificationDTO> notifications, long unreadCount) {
        this.success = success;
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.unreadCount = unreadCount;
        this.timestamp = System.currentTimeMillis();
    }

    // Constructeur pour erreur
    public NotificationResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
        this.notifications = new ArrayList<>();
        this.unreadCount = 0;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public List<NotificationDTO> getNotifications() { return notifications; }
    public void setNotifications(List<NotificationDTO> notifications) { this.notifications = notifications; }

    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
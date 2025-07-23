package com.library.dto;

import com.library.model.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.format.DateTimeFormatter;

public class NotificationDTO {
    private Long id;
    private String message;
    private String type;
    private boolean isRead;
    private String createdAt;
    private String timestamp;

    // Constructeur par défaut
    public NotificationDTO() {}

    // Constructeur à partir d'une Notification
    public NotificationDTO(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.type = notification.getType() != null ? notification.getType().toString() : "GENERAL_MESSAGE";
        this.isRead = notification.isRead();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (notification.getCreatedAt() != null) {
            this.createdAt = notification.getCreatedAt().format(formatter);
            this.timestamp = notification.getCreatedAt().toString();
        } else if (notification.getTimestamp() != null) {
            this.createdAt = notification.getTimestamp().format(formatter);
            this.timestamp = notification.getTimestamp().toString();
        }
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }
    
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
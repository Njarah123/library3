package com.library.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.library.enums.SubscriptionType;
import com.library.model.Subscription;

public class SubscriptionDTO {
    private Long id;
    private Long userId;
    private String userName;
    private String userEmail;
    private SubscriptionType subscriptionType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int booksRemaining;
    private boolean active;
    private boolean valid;
    private boolean hasQuotaRemaining;
    private long daysRemaining;

    public SubscriptionDTO() {
    }

    public SubscriptionDTO(Subscription subscription) {
        this.id = subscription.getId();
        this.userId = subscription.getUser().getId();
        this.userName = subscription.getUser().getName();
        this.userEmail = subscription.getUser().getEmail();
        this.subscriptionType = subscription.getSubscriptionType();
        this.startDate = subscription.getStartDate();
        this.endDate = subscription.getEndDate();
        this.booksRemaining = subscription.getBooksRemaining();
        this.active = subscription.isActive();
        this.valid = subscription.isValid();
        this.hasQuotaRemaining = subscription.hasQuotaRemaining();
        
        // Calculate days remaining
        LocalDateTime now = LocalDateTime.now();
        this.daysRemaining = ChronoUnit.DAYS.between(now, subscription.getEndDate());
    }
    
    /**
     * Static factory method to create a SubscriptionDTO from a Subscription entity
     */
    public static SubscriptionDTO fromSubscription(Subscription subscription) {
        return new SubscriptionDTO(subscription);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SubscriptionType getSubscriptionType() {
        return subscriptionType;
    }

    public void setSubscriptionType(SubscriptionType subscriptionType) {
        this.subscriptionType = subscriptionType;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public int getBooksRemaining() {
        return booksRemaining;
    }

    public void setBooksRemaining(int booksRemaining) {
        this.booksRemaining = booksRemaining;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getDaysRemaining() {
        return daysRemaining;
    }

    public void setDaysRemaining(long daysRemaining) {
        this.daysRemaining = daysRemaining;
    }
    
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isHasQuotaRemaining() {
        return hasQuotaRemaining;
    }

    public void setHasQuotaRemaining(boolean hasQuotaRemaining) {
        this.hasQuotaRemaining = hasQuotaRemaining;
    }
}
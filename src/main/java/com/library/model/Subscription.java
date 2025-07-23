package com.library.model;

import java.time.LocalDateTime;

import com.library.enums.SubscriptionType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user"})
@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "subscription_type", nullable = false)
    private SubscriptionType subscriptionType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "books_remaining", nullable = false)
    private int booksRemaining;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    // Constructors
    public Subscription() {
    }

    public Subscription(User user, SubscriptionType subscriptionType, LocalDateTime startDate, LocalDateTime endDate, int booksRemaining) {
        this.user = user;
        this.subscriptionType = subscriptionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.booksRemaining = booksRemaining;
    }

    // Helper methods
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isBefore(endDate);
    }

    public boolean hasQuotaRemaining() {
        return subscriptionType == SubscriptionType.PREMIUM || booksRemaining > 0;
    }

    public void decrementQuota() {
        if (subscriptionType != SubscriptionType.PREMIUM && booksRemaining > 0) {
            booksRemaining--;
        }
    }

    public void addQuota(int additionalBooks) {
        if (subscriptionType != SubscriptionType.PREMIUM) {
            booksRemaining += additionalBooks;
        }
    }

    public void extend(int additionalDays) {
        endDate = endDate.plusDays(additionalDays);
    }
}
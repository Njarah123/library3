package com.library.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "reservations")
public class Reservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "reservation_date", nullable = false)
    private LocalDateTime reservationDate;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.EN_ATTENTE;

    @Column(name = "notification_sent")
    private boolean notificationSent = false;

    @Column(name = "last_notification_date")
    private LocalDateTime lastNotificationDate;

    // Constructeur
    public Reservation() {
        this.reservationDate = LocalDateTime.now();
        this.expirationDate = this.reservationDate.plusDays(7); // Expire après 7 jours
    }

    // Méthodes de gestion du statut
    public void setStatus(ReservationStatus pret) {
        this.status = pret;
        if (status == ReservationStatus.PRET) {
            this.lastNotificationDate = LocalDateTime.now();
            this.notificationSent = true;
        }
    }

    public boolean isPending() {
        return this.status == ReservationStatus.EN_ATTENTE;
    }

    public boolean isReady() {
        return this.status == ReservationStatus.PRET;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expirationDate);
    }

    // Méthodes de validation
    @PrePersist
    protected void onCreate() {
        if (reservationDate == null) {
            reservationDate = LocalDateTime.now();
        }
        if (expirationDate == null) {
            expirationDate = reservationDate.plusDays(7);
        }
        if (status == null) {
            status = ReservationStatus.EN_ATTENTE;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        if (status == ReservationStatus.PRET && !notificationSent) {
            this.lastNotificationDate = LocalDateTime.now();
            this.notificationSent = true;
        }
    }

    public void setStatus(String string) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setStatus'");
    }
}
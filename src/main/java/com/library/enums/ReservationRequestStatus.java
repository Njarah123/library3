package com.library.enums;

public enum ReservationRequestStatus {
    ACTIVE,      // La réservation est active et en attente
    COMPLETED,   // La réservation a été complétée (livre emprunté)
    CANCELLED,   // La réservation a été annulée
    EXPIRED      // La réservation a expiré
}
package com.library.model;

public enum ReservationStatus {
    EN_ATTENTE,    // Réservation en attente de traitement
    PRET,          // Livre prêt à être récupéré
    EMPRUNTE,      // Réservation convertie en emprunt
    EXPIRE,        // Réservation expirée
    ANNULE,        // Réservation annulée
    REJETE         // Réservation rejetée
}
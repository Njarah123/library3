package com.library.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.model.Book;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    // Réservations par utilisateur
    List<Reservation> findByUserOrderByReservationDateDesc(User user);
    
    // Comptage des réservations actives par utilisateur
    int countByUserAndStatus(User user, String status);
    
    // Réservations en attente pour un livre
    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus enAttente);
    
    // Toutes les réservations actives
    List<Reservation> findByStatus(String status);
    
      Long countByStatus(String status);
    
    List<Reservation> findByUserAndStatus(User user, String status);
    
    // Vérifier si un utilisateur a déjà réservé un livre
    boolean existsByUserAndBookAndStatus(User user, Book book, String status);
    
    // Réservations par livre
    List<Reservation> findByBookOrderByReservationDateAsc(Book book);

    List<Reservation> findByBook(Book book);
    
    // Methods for notification system
    @Query("SELECT r FROM Reservation r WHERE r.expirationDate < :now AND r.processed = false")
    List<Reservation> findExpiredNotProcessed(@Param("now") LocalDateTime now);
    
    @Query("SELECT r FROM Reservation r WHERE r.book.available = true AND r.status = 'EN_ATTENTE' AND r.notified = false")
    List<Reservation> findAvailableNotNotified();
}
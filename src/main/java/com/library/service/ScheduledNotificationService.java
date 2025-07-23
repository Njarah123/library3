package com.library.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.model.Borrowing;
import com.library.model.Reservation;
import com.library.repository.BorrowingRepository;
import com.library.repository.ReservationRepository;

/**
 * Service responsible for scheduled notification tasks.
 * This service runs periodic checks and triggers appropriate notifications.
 */
@Service
public class ScheduledNotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledNotificationService.class);
    
    @Autowired
    private BorrowingRepository borrowingRepository;
    
    @Autowired
    private ReservationRepository reservationRepository;
    
    @Autowired
    private NotificationTriggerService notificationTriggerService;
    
    /**
     * Check for overdue books daily at midnight and send notifications
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkOverdueBooks() {
        logger.info("Running scheduled check for overdue books");
        LocalDateTime now = LocalDateTime.now();
        
        // Find all borrowings that are overdue but haven't been notified yet
        List<Borrowing> overdueBooks = borrowingRepository.findOverdueNotNotified(now);
        
        for (Borrowing borrowing : overdueBooks) {
            notificationTriggerService.onBookOverdue(borrowing);
            borrowing.setOverdueNotified(true);
            borrowingRepository.save(borrowing);
            logger.info("Sent overdue notification for book: {} to user: {}", 
                    borrowing.getBook().getTitle(), borrowing.getUser().getName());
        }
        
        logger.info("Completed overdue books check. Sent {} notifications", overdueBooks.size());
    }
    
    /**
     * Check for books due soon (in 2 days) and send reminders
     * Runs daily at 10:00 AM
     */
    @Scheduled(cron = "0 0 10 * * ?")
    @Transactional
    public void checkBooksDueSoon() {
        logger.info("Running scheduled check for books due soon");
        LocalDateTime twoDaysFromNow = LocalDateTime.now().plusDays(2);
        LocalDateTime threeDaysFromNow = LocalDateTime.now().plusDays(3);
        
        // Find all borrowings due in 2-3 days that haven't been notified yet
        List<Borrowing> booksDueSoon = borrowingRepository.findDueSoonNotNotified(twoDaysFromNow, threeDaysFromNow);
        
        for (Borrowing borrowing : booksDueSoon) {
            notificationTriggerService.onBookDueSoon(borrowing);
            borrowing.setDueSoonNotified(true);
            borrowingRepository.save(borrowing);
            logger.info("Sent due soon reminder for book: {} to user: {}", 
                    borrowing.getBook().getTitle(), borrowing.getUser().getName());
        }
        
        logger.info("Completed books due soon check. Sent {} notifications", booksDueSoon.size());
    }
    
    /**
     * Check for expired reservations daily at 1:00 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkExpiredReservations() {
        logger.info("Running scheduled check for expired reservations");
        LocalDateTime now = LocalDateTime.now();
        
        // Find all reservations that have expired but haven't been processed yet
        List<Reservation> expiredReservations = reservationRepository.findExpiredNotProcessed(now);
        
        for (Reservation reservation : expiredReservations) {
            notificationTriggerService.onReservationExpired(reservation);
            reservation.setExpired(true);
            reservation.setProcessed(true);
            reservationRepository.save(reservation);
            logger.info("Processed expired reservation for book: {} by user: {}", 
                    reservation.getBook().getTitle(), reservation.getUser().getName());
        }
        
        logger.info("Completed expired reservations check. Processed {} reservations", expiredReservations.size());
    }
    
    /**
     * Check for available books with pending reservations every hour
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void checkAvailableReservations() {
        logger.info("Running scheduled check for available reservations");
        
        // Find all reservations where the book is now available but the user hasn't been notified
        List<Reservation> availableReservations = reservationRepository.findAvailableNotNotified();
        
        for (Reservation reservation : availableReservations) {
            notificationTriggerService.onReservationAvailable(reservation);
            reservation.setNotified(true);
            reservationRepository.save(reservation);
            logger.info("Sent availability notification for book: {} to user: {}", 
                    reservation.getBook().getTitle(), reservation.getUser().getName());
        }
        
        logger.info("Completed available reservations check. Sent {} notifications", availableReservations.size());
    }
    
    /**
     * Send weekly reading activity summary to users every Sunday at 8:00 AM
     */
    @Scheduled(cron = "0 0 8 * * SUN")
    @Transactional
    public void sendWeeklyReadingSummary() {
        logger.info("Sending weekly reading summaries");
        
        // Implementation would involve:
        // 1. Getting all users
        // 2. For each user, calculating their reading activity for the week
        // 3. Sending a personalized notification with their stats
        
        logger.info("Completed sending weekly reading summaries");
    }
    
    /**
     * Check for low book stock daily at 2:00 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void checkLowBookStock() {
        logger.info("Running scheduled check for low book stock");
        
        // Implementation would involve:
        // 1. Finding all books with stock below a threshold
        // 2. For each book, sending a notification to librarians
        
        logger.info("Completed low book stock check");
    }
}
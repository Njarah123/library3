package com.library.service;

import com.library.enums.BorrowingStatus;
import com.library.enums.NotificationType;
import com.library.model.Borrowing;
import com.library.model.User;
import com.library.repository.BorrowingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class NotificationScheduler {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<Borrowing> overdueBorrowings = borrowingRepository.findByStatusAndDueDateBefore(BorrowingStatus.EMPRUNTE, now);

        for (Borrowing borrowing : overdueBorrowings) {
            String librarianMessage = borrowing.getUser().getName() + " (" + borrowing.getUser().getUserType() + ") has an overdue book: '" + borrowing.getBook().getTitle() + "' (Due date: " + borrowing.getDueDate().toLocalDate() + ").";
            List<User> librarians = userService.getAllLibrarians();
            for (User librarian : librarians) {
                notificationService.createNotification(librarian, librarianMessage, NotificationType.OVERDUE_REMINDER);
            }
            // Also notify the user
            String userMessage = "Reminder: '" + borrowing.getBook().getTitle() + "' is overdue (Due date: " + borrowing.getDueDate().toLocalDate() + ").";
            notificationService.createNotification(borrowing.getUser(), userMessage, NotificationType.OVERDUE_REMINDER);
        }
    }

    // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * ?")
    @Transactional
    public void sendDueSoonReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        List<Borrowing> dueSoonBorrowings = borrowingRepository.findByStatusAndDueDateBetween(BorrowingStatus.EMPRUNTE, now, tomorrow);

        for (Borrowing borrowing : dueSoonBorrowings) {
            String message = "Your borrowed book '" + borrowing.getBook().getTitle() + "' is due tomorrow.";
            notificationService.createNotification(borrowing.getUser(), message, NotificationType.DUE_SOON_REMINDER);
        }
    }
}
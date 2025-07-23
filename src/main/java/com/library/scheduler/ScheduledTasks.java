package com.library.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.library.service.BorrowingService;

/**
 * Class containing scheduled tasks for the library application.
 * These tasks run automatically at specified intervals.
 */
@Component
public class ScheduledTasks {
    
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasks.class);
    
    @Autowired
    private BorrowingService borrowingService;
    
    /**
     * Scheduled task to check for overdue books and books due soon.
     * Runs daily at 8:00 AM.
     * 
     * The cron expression "0 0 8 * * ?" means:
     * - 0 seconds
     * - 0 minutes
     * - 8 hours (8:00 AM)
     * - Every day of the month
     * - Every month
     * - Every day of the week
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void checkDueBooks() {
        logger.info("Running scheduled task: checking for overdue books and books due soon");
        try {
            borrowingService.checkAndNotifyDueBooks();
            logger.info("Scheduled task completed successfully");
        } catch (Exception e) {
            logger.error("Error running scheduled task for checking due books", e);
        }
    }
}
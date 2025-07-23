package com.library.scheduler;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.library.model.Subscription;
import com.library.model.User;
import com.library.service.NotificationService;
import com.library.service.SubscriptionService;

/**
 * Scheduler for checking subscription expirations and sending notifications
 */
@Component
public class SubscriptionExpirationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionExpirationScheduler.class);
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private NotificationService notificationService;
    
    /**
     * Check for subscriptions expiring in the next 3 days and send notifications
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkExpiringSubscriptions() {
        logger.info("Running scheduled task to check for expiring subscriptions");
        
        // Get subscriptions expiring in the next 3 days
        List<Subscription> expiringSubscriptions = subscriptionService.getSubscriptionsExpiringSoon(3);
        
        for (Subscription subscription : expiringSubscriptions) {
            User user = subscription.getUser();
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
            
            // Send notification to user
            notificationService.createSubscriptionExpiringSoonNotification(user, daysRemaining);
            
            logger.info("Sent expiration notification to user {} for subscription expiring in {} days", 
                    user.getUsername(), daysRemaining);
        }
        
        logger.info("Completed checking for expiring subscriptions. Found {} subscriptions expiring soon", 
                expiringSubscriptions.size());
    }
    
    /**
     * Check for subscriptions with low book quota and send notifications
     * Runs daily at 1 AM
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void checkLowQuotaSubscriptions() {
        logger.info("Running scheduled task to check for subscriptions with low book quota");
        
        // Get subscriptions with zero books remaining
        List<Subscription> lowQuotaSubscriptions = subscriptionService.getSubscriptionsWithZeroBooksRemaining();
        
        for (Subscription subscription : lowQuotaSubscriptions) {
            User user = subscription.getUser();
            
            // Send notification to user
            notificationService.createSubscriptionQuotaLowNotification(user, 0);
            
            logger.info("Sent low quota notification to user {} for subscription with zero books remaining", 
                    user.getUsername());
        }
        
        logger.info("Completed checking for subscriptions with low quota. Found {} subscriptions with zero books remaining", 
                lowQuotaSubscriptions.size());
    }
    
    /**
     * Check for expired subscriptions and mark them as inactive
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void deactivateExpiredSubscriptions() {
        logger.info("Running scheduled task to deactivate expired subscriptions");
        
        LocalDateTime now = LocalDateTime.now();
        List<Subscription> activeSubscriptions = subscriptionService.getAllActiveSubscriptions();
        int expiredCount = 0;
        
        for (Subscription subscription : activeSubscriptions) {
            if (now.isAfter(subscription.getEndDate())) {
                // Mark subscription as inactive
                subscription.setActive(false);
                subscriptionService.saveSubscription(subscription);
                
                // Send notification to user
                notificationService.createSubscriptionExpirationNotification(
                        subscription.getUser(), subscription.getSubscriptionType());
                
                expiredCount++;
                logger.info("Deactivated expired subscription for user {}", 
                        subscription.getUser().getUsername());
            }
        }
        
        logger.info("Completed deactivating expired subscriptions. Deactivated {} subscriptions", expiredCount);
    }
    
    /**
     * Check for subscriptions expiring in the next 7 days and send renewal reminders
     * Runs daily at 3 AM
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void sendRenewalReminders() {
        logger.info("Running scheduled task to send subscription renewal reminders");
        
        // Get subscriptions expiring in the next 7 days
        List<Subscription> expiringSubscriptions = subscriptionService.getSubscriptionsExpiringSoon(7);
        
        for (Subscription subscription : expiringSubscriptions) {
            User user = subscription.getUser();
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), subscription.getEndDate());
            
            // Send renewal reminder notification to user
            notificationService.createSubscriptionRenewalReminderNotification(
                    user, subscription.getSubscriptionType(), daysRemaining);
            
            logger.info("Sent renewal reminder to user {} for subscription expiring in {} days",
                    user.getUsername(), daysRemaining);
        }
        
        logger.info("Completed sending renewal reminders. Sent {} reminders",
                expiringSubscriptions.size());
    }
}
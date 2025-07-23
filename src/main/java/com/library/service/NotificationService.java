package com.library.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.NotificationType;
import com.library.enums.SubscriptionType;
import com.library.enums.UserType;
import com.library.model.Notification;
import com.library.model.User;
import com.library.repository.NotificationRepository;

/**
 * Service for managing notifications in the library system.
 * Provides methods for creating, retrieving, and managing notifications.
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserService userService;

    /**
     * Create a notification for a specific user
     */
    @Transactional
    public Notification createNotification(User user, String message, NotificationType type) {
        return createNotification(user, message, type, null);
    }

    /**
     * Create a notification for a specific user with trigger user information
     */
    @Transactional
    public Notification createNotification(User user, String message, NotificationType type, User triggeredByUser) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setType(type);
        notification.setTriggeredByUser(triggeredByUser);
        return notificationRepository.save(notification);
    }

    /**
     * Create a notification for all users with a specific role
     */
    @Transactional
    public void createNotificationForRole(String role, String message, NotificationType type) {
        createNotificationForRole(role, message, type, null);
    }

    /**
     * Create a notification for all users with a specific role, excluding a specific user
     */
    @Transactional
    public void createNotificationForRole(String role, String message, NotificationType type, User excludedUser) {
        createNotificationForRole(role, message, type, excludedUser, null);
    }

    /**
     * Create a notification for all users with a specific role, excluding a specific user, with trigger user
     */
    @Transactional
    public void createNotificationForRole(String role, String message, NotificationType type, User excludedUser, User triggeredByUser) {
        List<User> users = userService.findByRole(role);
        for (User user : users) {
            if (excludedUser != null && user.getId().equals(excludedUser.getId())) {
                continue;
            }
            createNotification(user, message, type, triggeredByUser);
        }
    }

    /**
     * Create a notification for all users with a specific user type
     */
    @Transactional
    public void createNotificationForUserType(UserType userType, String message, NotificationType type) {
        createNotificationForUserType(userType, message, type, null, null);
    }

    /**
     * Create a notification for all users with a specific user type, excluding a specific user
     */
    @Transactional
    public void createNotificationForUserType(UserType userType, String message, NotificationType type, User excludedUser) {
        createNotificationForUserType(userType, message, type, excludedUser, null);
    }

    /**
     * Create a notification for all users with a specific user type, excluding a specific user, with trigger user
     */
    @Transactional
    public void createNotificationForUserType(UserType userType, String message, NotificationType type, User excludedUser, User triggeredByUser) {
        List<User> users = userService.findByUserType(userType);
        for (User user : users) {
            if (excludedUser != null && user.getId().equals(excludedUser.getId())) {
                continue;
            }
            createNotification(user, message, type, triggeredByUser);
        }
    }

    /**
     * Get all unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    /**
     * Get all notifications for a user
     */
    public List<Notification> getAllNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Get notifications by user (alias for getAllNotifications)
     */
    public List<Notification> getNotificationsByUser(User user) {
        return getAllNotifications(user);
    }

    /**
     * Get the count of unread notifications for a user
     */
    public long getUnreadNotificationCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    /**
     * Mark a notification as read
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    /**
     * Mark a notification as read with user verification
     */
    @Transactional
    public boolean markAsRead(Long notificationId, User user) {
        return notificationRepository.findById(notificationId)
            .filter(notification -> notification.getUser().getId().equals(user.getId()))
            .map(notification -> {
                notification.setRead(true);
                notificationRepository.save(notification);
                return true;
            })
            .orElse(false);
    }

    /**
     * Mark all notifications as read for a user
     */
    @Transactional
    public int markAllAsRead(User user) {
        try {
            // Utiliser une requête de mise à jour en masse pour plus d'efficacité
            int updatedCount = notificationRepository.markAllAsReadForUser(user);
            return updatedCount;
        } catch (Exception e) {
            // Relancer l'exception pour que le contrôleur puisse la gérer
            throw e;
        }
    }

    /**
     * Delete a notification
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    /**
     * Delete a notification with user verification
     */
    @Transactional
    public boolean deleteNotification(Long notificationId, User user) {
        return notificationRepository.findById(notificationId)
            .filter(notification -> notification.getUser().getId().equals(user.getId()))
            .map(notification -> {
                notificationRepository.delete(notification);
                return true;
            })
            .orElse(false);
    }

    /**
     * Delete all notifications for a user
     */
    @Transactional
    public void deleteAllNotifications(User user) {
        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);
        notificationRepository.deleteAll(notifications);
    }

    /**
     * Delete all read notifications for a user
     */
    @Transactional
    public void deleteReadNotifications(User user) {
        List<Notification> readNotifications = notificationRepository.findByUserAndIsReadTrueOrderByCreatedAtDesc(user);
        notificationRepository.deleteAll(readNotifications);
    }
    
    /**
     * Get the latest notifications for a user, limited to a specific number
     * This method is used as a fallback when there are issues with date fields
     */
    public List<Notification> getLatestNotifications(User user, int limit) {
        List<Notification> allNotifications = notificationRepository.findByUserOrderByIdDesc(user);
        if (allNotifications.size() <= limit) {
            return allNotifications;
        }
        return allNotifications.subList(0, limit);
    }
    
    /**
     * Create a notification for a subscription purchase
     */
    @Transactional
    public Notification createSubscriptionPurchaseNotification(User user, SubscriptionType subscriptionType) {
        String message = "You have successfully purchased a " + subscriptionType.getDisplayName() +
                " subscription for " + subscriptionType.getPrice() + " Ar.";
        return createNotification(user, message, NotificationType.SUBSCRIPTION_PURCHASED);
    }
    
    /**
     * Create a notification for a subscription cancellation
     */
    @Transactional
    public Notification createSubscriptionCancellationNotification(User user) {
        String message = "Your subscription has been cancelled.";
        return createNotification(user, message, NotificationType.SUBSCRIPTION_CANCELLED);
    }
    
    /**
     * Create a notification for a subscription expiration
     */
    @Transactional
    public Notification createSubscriptionExpirationNotification(User user, SubscriptionType subscriptionType) {
        String message = "Your " + subscriptionType.getDisplayName() + " subscription has expired.";
        return createNotification(user, message, NotificationType.SUBSCRIPTION_EXPIRED);
    }
    
    /**
     * Create a notification for a subscription renewal
     */
    @Transactional
    public Notification createSubscriptionRenewalNotification(User user, SubscriptionType subscriptionType) {
        String message = "Your " + subscriptionType.getDisplayName() + " subscription has been renewed.";
        return createNotification(user, message, NotificationType.SUBSCRIPTION_RENEWED);
    }
    
    /**
     * Create a notification for a subscription quota running low
     */
    @Transactional
    public Notification createSubscriptionQuotaLowNotification(User user, int booksRemaining) {
        String message = "You have only " + booksRemaining + " books remaining in your subscription quota.";
        return createNotification(user, message, NotificationType.SUBSCRIPTION_QUOTA_LOW);
    }
    
    /**
     * Create a notification for a subscription expiring soon
     */
    @Transactional
    public Notification createSubscriptionExpiringSoonNotification(User user, long daysRemaining) {
        String message = "Your subscription will expire in " + daysRemaining + " days.";
        return createNotification(user, message, NotificationType.SUBSCRIPTION_EXPIRING_SOON);
    }
    
    /**
     * Create a notification for a subscription renewal reminder
     */
    @Transactional
    public Notification createSubscriptionRenewalReminderNotification(User user, SubscriptionType subscriptionType, long daysRemaining) {
        String message = "Your " + subscriptionType.getDisplayName() + " subscription will expire in " + daysRemaining +
                " days. Renew now to avoid interruption of service.";
        return createNotification(user, message, NotificationType.SUBSCRIPTION_RENEWAL_REMINDER);
    }
    
    /**
     * Create a notification for a subscription upgrade or downgrade
     */
    @Transactional
    public Notification createSubscriptionChangeNotification(User user, SubscriptionType oldType, SubscriptionType newType) {
        boolean isUpgrade = newType.getPrice() > oldType.getPrice();
        String action = isUpgrade ? "upgraded" : "downgraded";
        
        String message = "Your subscription has been " + action + " from " + oldType.getDisplayName() +
                " to " + newType.getDisplayName() + ".";
        
        return createNotification(user, message, NotificationType.SUBSCRIPTION_RENEWED);
    }
}
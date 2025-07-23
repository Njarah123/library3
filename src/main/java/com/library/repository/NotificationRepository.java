package com.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.NotificationType;
import com.library.model.Notification;
import com.library.model.User;

/**
 * Repository for accessing notification data in the database.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all unread notifications for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    /**
     * Update all unread notifications for a user to read status
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user = :user AND n.isRead = false")
    int markAllAsReadForUser(@Param("user") User user);
    
    /**
     * Find all read notifications for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserAndIsReadTrueOrderByCreatedAtDesc(User user);
    
    /**
     * Find all notifications for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    /**
     * Find all notifications of a specific type for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, NotificationType type);
    
    /**
     * Find all unread notifications of a specific type for a user, ordered by creation date (newest first)
     */
    List<Notification> findByUserAndTypeAndIsReadFalseOrderByCreatedAtDesc(User user, NotificationType type);
    
    /**
     * Count all unread notifications for a user
     */
    long countByUserAndIsReadFalse(User user);
    
    /**
     * Count all unread notifications of a specific type for a user
     */
    long countByUserAndTypeAndIsReadFalse(User user, NotificationType type);
    
    /**
     * Find all notifications for a user, ordered by ID (newest first)
     * This is used as a fallback when there are issues with date fields
     */
    List<Notification> findByUserOrderByIdDesc(User user);
}
package com.library.repository;

import com.library.model.Subscription;
import com.library.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    /**
     * Find the active subscription for a user
     * @param user The user
     * @return The active subscription if exists
     */
    Optional<Subscription> findByUserAndActiveTrue(User user);
    
    /**
     * Find all active subscriptions for a user
     * @param user The user
     * @return List of active subscriptions
     */
    List<Subscription> findAllByUserAndActiveTrue(User user);
    
    /**
     * Find all subscriptions for a user
     * @param user The user
     * @return List of all subscriptions
     */
    List<Subscription> findAllByUser(User user);
    
    /**
     * Find all active subscriptions that are about to expire
     * @param expiryDate The date to check against
     * @return List of subscriptions expiring soon
     */
    @Query("SELECT s FROM Subscription s WHERE s.active = true AND s.endDate <= :expiryDate")
    List<Subscription> findAllActiveSubscriptionsExpiringBefore(@Param("expiryDate") LocalDateTime expiryDate);
    
    /**
     * Find all active subscriptions with zero books remaining
     * @return List of subscriptions with no books remaining
     */
    @Query("SELECT s FROM Subscription s WHERE s.active = true AND s.booksRemaining = 0 AND s.subscriptionType <> com.library.enums.SubscriptionType.PREMIUM")
    List<Subscription> findAllActiveSubscriptionsWithZeroBooksRemaining();
    
    /**
     * Count the number of active subscriptions for a user
     * @param user The user
     * @return The count of active subscriptions
     */
    long countByUserAndActiveTrue(User user);
    
    /**
     * Find all active subscriptions for students
     * @return List of active student subscriptions
     */
    @Query("SELECT s FROM Subscription s JOIN s.user u WHERE s.active = true AND s.endDate > CURRENT_TIMESTAMP AND u.userType = com.library.enums.UserType.STUDENT AND u.username = 'joannahasina'")
    List<Subscription> findAllActiveStudentSubscriptions();
}
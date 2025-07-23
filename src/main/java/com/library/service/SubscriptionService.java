package com.library.service;

import java.util.List;
import java.util.Optional;

import com.library.enums.SubscriptionType;
import com.library.model.Subscription;
import com.library.model.User;

public interface SubscriptionService {
    
    /**
     * Create a new subscription for a user
     * @param user The user
     * @param subscriptionType The subscription type
     * @return The created subscription
     */
    Subscription createSubscription(User user, SubscriptionType subscriptionType);
    
    /**
     * Get the active subscription for a user
     * @param user The user
     * @return The active subscription if exists
     */
    Optional<Subscription> getActiveSubscription(User user);
    
    /**
     * Get a subscription by its ID
     * @param id The subscription ID
     * @return The subscription if exists
     */
    Optional<Subscription> getSubscriptionById(Long id);
    
    /**
     * Check if a user has an active subscription
     * @param user The user
     * @return True if the user has an active subscription
     */
    boolean hasActiveSubscription(User user);
    
    /**
     * Check if a user can borrow a book based on their subscription
     * @param user The user
     * @return True if the user can borrow a book
     */
    boolean canBorrowBook(User user);
    
    /**
     * Decrement the book quota for a user's subscription
     * @param user The user
     * @return True if the quota was decremented successfully
     */
    boolean decrementBookQuota(User user);
    
    /**
     * Add books to a user's subscription quota
     * @param user The user
     * @param additionalBooks The number of books to add
     * @return True if the books were added successfully
     */
    boolean addBooksToQuota(User user, int additionalBooks);
    
    /**
     * Extend a user's subscription
     * @param user The user
     * @param additionalDays The number of days to extend
     * @return True if the subscription was extended successfully
     */
    boolean extendSubscription(User user, int additionalDays);
    
    /**
     * Cancel a user's subscription
     * @param user The user
     * @return True if the subscription was cancelled successfully
     */
    boolean cancelSubscription(User user);
    
    /**
     * Get all subscriptions for a user
     * @param user The user
     * @return List of all subscriptions
     */
    List<Subscription> getAllSubscriptions(User user);
    
    /**
     * Get all active subscriptions
     * @return List of all active subscriptions
     */
    List<Subscription> getAllActiveSubscriptions();
    
    /**
     * Get all active subscriptions for students
     * @return List of all active student subscriptions
     */
    List<Subscription> getAllActiveStudentSubscriptions();
    
    /**
     * Get all subscriptions
     * @return List of all subscriptions
     */
    List<Subscription> getAllSubscriptions();
    
    /**
     * Get all active subscriptions expiring soon
     * @param daysThreshold The number of days threshold
     * @return List of subscriptions expiring soon
     */
    List<Subscription> getSubscriptionsExpiringSoon(int daysThreshold);
    
    /**
     * Get all active subscriptions with zero books remaining
     * @return List of subscriptions with no books remaining
     */
    List<Subscription> getSubscriptionsWithZeroBooksRemaining();
    
    /**
     * Save a subscription
     * @param subscription The subscription to save
     * @return The saved subscription
     */
    Subscription saveSubscription(Subscription subscription);
}
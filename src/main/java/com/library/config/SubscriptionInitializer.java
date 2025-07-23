package com.library.config;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.library.enums.SubscriptionType;
import com.library.enums.UserType;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.service.SubscriptionService;
import com.library.service.UserService;

/**
 * Initializes default subscriptions for existing users who don't have one
 */
@Component
public class SubscriptionInitializer {

    private static final Logger logger = LoggerFactory.getLogger(SubscriptionInitializer.class);
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    /**
     * Create default subscriptions for existing users when the application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultSubscriptions() {
        logger.info("Subscription initializer running...");
        
        // Get all student users
        List<User> students = userService.getAllStudents();
        int count = 0;
        
        // Log the number of students without subscriptions
        for (User student : students) {
            Optional<Subscription> existingSubscription = subscriptionService.getActiveSubscription(student);
            if (!existingSubscription.isPresent()) {
                count++;
            }
        }
        
        logger.info("Found {} students without active subscriptions", count);
        logger.info("Students will be prompted to choose a subscription when they attempt to borrow a book");
        
        // We no longer automatically create subscriptions
        // Instead, students will be shown subscription options when they try to borrow a book
    }
}
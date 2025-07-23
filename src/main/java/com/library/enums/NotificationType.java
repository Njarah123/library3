package com.library.enums;

/**
 * Enum representing different types of notifications in the library system.
 * Each notification type is relevant to specific user roles.
 */
public enum NotificationType {
    // Common notifications
    GENERAL_MESSAGE,          // General message for all users
    
    // Book-related notifications
    NEW_BOOK_ADDED,           // When a new book is added to the library
    BOOK_UPDATE,              // When a book's information is updated
    BOOK_RECOMMENDATION,      // Book recommendations for users
    
    // Borrowing-related notifications
    BOOK_BORROWED,            // When a book is borrowed
    BOOK_RETURNED,            // When a book is returned
    OVERDUE_REMINDER,         // Reminder for overdue books
    DUE_SOON_REMINDER,        // Reminder for books due soon
    FINE_ADDED,               // When a fine is added to a user's account
    FINE_PAID,                // When a fine is paid
    
    // Reservation-related notifications
    RESERVATION_AVAILABLE,    // When a reserved book becomes available
    RESERVATION_CONFIRMED,    // When a reservation is confirmed
    RESERVATION_CANCELLED,    // When a reservation is cancelled
    RESERVATION_EXPIRED,      // When a reservation expires
    
    // Librarian-specific notifications
    LOW_BOOK_STOCK,           // When a book's stock is running low
    BOOK_DAMAGE_REPORTED,     // When a book damage is reported
    NEW_MEMBER_REGISTERED,    // When a new member registers
    BOOK_LOST_REPORTED,       // When a book is reported as lost
    
    // Staff-specific notifications
    APPROVAL_REQUIRED,        // When approval is required for an action
    TASK_ASSIGNED,            // When a task is assigned to a staff member
    
    // Student-specific notifications
    ACCOUNT_STATUS_CHANGE,    // When a student's account status changes
    BOOK_RENEWAL_CONFIRMED,   // When a book renewal is confirmed
    BOOK_RENEWAL_REJECTED,    // When a book renewal is rejected
    
    // Subscription-related notifications
    SUBSCRIPTION_PURCHASED,   // When a subscription is purchased
    SUBSCRIPTION_EXPIRED,     // When a subscription expires
    SUBSCRIPTION_RENEWED,     // When a subscription is renewed
    SUBSCRIPTION_CANCELLED,   // When a subscription is cancelled
    SUBSCRIPTION_QUOTA_LOW,   // When a subscription's book quota is running low
    SUBSCRIPTION_EXPIRING_SOON, // When a subscription is about to expire
    SUBSCRIPTION_RENEWAL_REMINDER // Reminder to renew subscription before it expires
}
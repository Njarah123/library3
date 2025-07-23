package com.library.service;

import com.library.model.Payment;
import com.library.model.TransactionLog;
import com.library.model.User;

import java.util.List;

/**
 * Service interface for logging payment transactions and security events.
 */
public interface TransactionLoggerService {
    
    /**
     * Log a payment transaction.
     * 
     * @param payment The payment being processed
     * @param user The user performing the transaction
     * @param action The action being performed (e.g., "PAYMENT_INITIATED", "PAYMENT_COMPLETED")
     * @param details Additional details about the transaction
     * @param ipAddress The IP address of the user
     * @return The created transaction log
     */
    TransactionLog logPaymentTransaction(Payment payment, User user, String action, String details, String ipAddress);
    
    /**
     * Log a security event related to payments.
     * 
     * @param user The user involved in the security event
     * @param action The security action (e.g., "SUSPICIOUS_PAYMENT_ATTEMPT", "MULTIPLE_FAILED_PAYMENTS")
     * @param details Additional details about the security event
     * @param ipAddress The IP address of the user
     * @return The created transaction log
     */
    TransactionLog logSecurityEvent(User user, String action, String details, String ipAddress);
    
    /**
     * Get all transaction logs for a specific payment.
     * 
     * @param payment The payment
     * @return A list of transaction logs
     */
    List<TransactionLog> getTransactionLogsForPayment(Payment payment);
    
    /**
     * Get all transaction logs for a specific user.
     * 
     * @param user The user
     * @return A list of transaction logs
     */
    List<TransactionLog> getTransactionLogsForUser(User user);
    
    /**
     * Get all security event logs for a specific user.
     * 
     * @param user The user
     * @return A list of security event logs
     */
    List<TransactionLog> getSecurityEventsForUser(User user);
    
    /**
     * Get a transaction log by its ID.
     * 
     * @param id The transaction log ID
     * @return The transaction log
     */
    TransactionLog getTransactionLogById(Long id);
    
    /**
     * Delete a transaction log.
     * 
     * @param id The transaction log ID
     */
    void deleteTransactionLog(Long id);
}
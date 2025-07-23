package com.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.model.Payment;
import com.library.model.TransactionLog;
import com.library.model.User;

/**
 * Repository for transaction log operations.
 */
@Repository
public interface TransactionLogRepository extends JpaRepository<TransactionLog, Long> {
    
    /**
     * Find all transaction logs for a specific payment.
     * 
     * @param payment The payment
     * @return A list of transaction logs
     */
    List<TransactionLog> findByPaymentOrderByTimestampDesc(Payment payment);
    
    /**
     * Find all transaction logs for a specific user.
     * 
     * @param user The user
     * @return A list of transaction logs
     */
    List<TransactionLog> findByUserOrderByTimestampDesc(User user);
    
    /**
     * Find all security event logs for a specific user.
     * 
     * @param user The user
     * @param isSecurityEvent Whether the log is a security event
     * @return A list of security event logs
     */
    List<TransactionLog> findByUserAndIsSecurityEventOrderByTimestampDesc(User user, Boolean isSecurityEvent);
    
    /**
     * Find all transaction logs for a specific action.
     * 
     * @param action The action
     * @return A list of transaction logs
     */
    List<TransactionLog> findByActionOrderByTimestampDesc(String action);
    
    /**
     * Find all transaction logs for a specific payment method.
     * 
     * @param paymentMethod The payment method
     * @return A list of transaction logs
     */
    List<TransactionLog> findByPaymentMethodOrderByTimestampDesc(String paymentMethod);
    
    /**
     * Find all transaction logs for a specific payment type.
     * 
     * @param paymentType The payment type
     * @return A list of transaction logs
     */
    List<TransactionLog> findByPaymentTypeOrderByTimestampDesc(String paymentType);
    
    /**
     * Find all transaction logs for a specific payment status.
     * 
     * @param paymentStatus The payment status
     * @return A list of transaction logs
     */
    List<TransactionLog> findByPaymentStatusOrderByTimestampDesc(String paymentStatus);
}
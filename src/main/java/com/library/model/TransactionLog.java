package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import com.library.enums.PaymentType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * Entity representing a transaction log entry for payment transactions.
 */
@Data
@Entity
@Table(name = "transaction_logs")
public class TransactionLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "action", nullable = false)
    private String action;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
    
    @Column(name = "amount", precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "is_security_event")
    private Boolean isSecurityEvent = false;
    
    /**
     * Create a new transaction log entry.
     */
    public TransactionLog() {
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * Create a new transaction log entry for a payment.
     * 
     * @param payment The payment
     * @param action The action
     * @param details The details
     */
    public TransactionLog(Payment payment, String action, String details) {
        this();
        this.payment = payment;
        this.user = payment.getUser();
        this.action = action;
        this.details = details;
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentType = payment.getPaymentType();
        this.paymentStatus = payment.getStatus();
    }
    
    /**
     * Create a new transaction log entry for a user.
     * 
     * @param user The user
     * @param action The action
     * @param details The details
     */
    public TransactionLog(User user, String action, String details) {
        this();
        this.user = user;
        this.action = action;
        this.details = details;
    }
    
    /**
     * Create a new security event log entry.
     * 
     * @param user The user
     * @param action The action
     * @param details The details
     * @param ipAddress The IP address
     * @param userAgent The user agent
     */
    public TransactionLog(User user, String action, String details, String ipAddress, String userAgent) {
        this(user, action, details);
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.isSecurityEvent = true;
    }
}
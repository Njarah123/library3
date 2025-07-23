package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "payments")
public class Payment {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;
    
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "invoice_number", unique = true)
    private String invoiceNumber;
    
    @Column(name = "billing_address")
    private String billingAddress;
    
    @Column(name = "payment_details", columnDefinition = "TEXT")
    private String paymentDetails;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type")
    private PaymentType paymentType;
    
    @ManyToOne
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;
    
    @Column(name = "is_refund")
    private Boolean isRefund = false;
    
    @PrePersist
    public void prePersist() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        
        if (invoiceNumber == null) {
            // Generate a unique invoice number with format INV-YYYYMMDD-XXXX
            String datePart = LocalDateTime.now().toString().substring(0, 10).replace("-", "");
            String randomPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            invoiceNumber = "INV-" + datePart + "-" + randomPart;
        }
    }
    
    // Constructors
    public Payment() {
    }
    
    public Payment(User user, BigDecimal amount, PaymentMethod paymentMethod) {
        this.user = user;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }
    
    public Payment(User user, BigDecimal amount, PaymentMethod paymentMethod, String description, String billingAddress) {
        this.user = user;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.description = description;
        this.billingAddress = billingAddress;
        this.status = PaymentStatus.PENDING;
    }
}
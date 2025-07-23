package com.library.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import com.library.enums.PaymentType;
import com.library.enums.SubscriptionType;
import com.library.model.Payment;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.repository.PaymentRepository;
import com.library.repository.UserRepository;
import com.library.service.NotificationTriggerService;
import com.library.service.PaymentService;
import com.library.service.TransactionLoggerService;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private NotificationTriggerService notificationTriggerService;
    
    @Autowired
    private TransactionLoggerService transactionLoggerService;
    
    @Override
    @Transactional
    public Payment processPayment(User user, BigDecimal amount, String paymentMethodStr) {
        // Convert string payment method to enum
        PaymentMethod paymentMethod = PaymentMethod.getByCode(paymentMethodStr);
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.OTHER;
        }
        
        // Create a new payment
        Payment payment = new Payment(user, amount, paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setDescription("Fine payment");
        payment.setPaymentType(PaymentType.FINE);
        
        // Generate a transaction ID
        String transactionId = "TXN" + System.currentTimeMillis();
        payment.setTransactionId(transactionId);
        
        // Save the payment
        payment = paymentRepository.save(payment);
        
        // Update the user's fine amount
        BigDecimal currentFineAmount = user.getAccount().getFineAmount();
        if (currentFineAmount == null) {
            currentFineAmount = BigDecimal.ZERO;
        }
        
        // Calculate the new fine amount (ensure it doesn't go below zero)
        BigDecimal newFineAmount = currentFineAmount.subtract(amount);
        if (newFineAmount.compareTo(BigDecimal.ZERO) < 0) {
            newFineAmount = BigDecimal.ZERO;
        }
        
        user.getAccount().setFineAmount(newFineAmount);
        userRepository.save(user);
        
        // Trigger a notification
        notificationTriggerService.onFinePaid(user, amount.doubleValue());
        
        return payment;
    }
    
    @Override
    @Transactional
    public Payment processPayment(User user, BigDecimal amount, String paymentMethodStr, String description, String billingAddress) {
        // Convert string payment method to enum
        PaymentMethod paymentMethod = PaymentMethod.getByCode(paymentMethodStr);
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.OTHER;
        }
        
        // Create a new payment with additional details
        Payment payment = new Payment(user, amount, paymentMethod, description, billingAddress);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setPaymentType(PaymentType.FINE);
        
        // Generate a transaction ID
        String transactionId = "TXN" + System.currentTimeMillis();
        payment.setTransactionId(transactionId);
        
        // Save the payment
        payment = paymentRepository.save(payment);
        
        // Update the user's fine amount
        BigDecimal currentFineAmount = user.getAccount().getFineAmount();
        if (currentFineAmount == null) {
            currentFineAmount = BigDecimal.ZERO;
        }
        
        // Calculate the new fine amount (ensure it doesn't go below zero)
        BigDecimal newFineAmount = currentFineAmount.subtract(amount);
        if (newFineAmount.compareTo(BigDecimal.ZERO) < 0) {
            newFineAmount = BigDecimal.ZERO;
        }
        
        user.getAccount().setFineAmount(newFineAmount);
        userRepository.save(user);
        
        // Trigger a notification
        notificationTriggerService.onFinePaid(user, amount.doubleValue());
        
        return payment;
    }

    @Override
    public List<Payment> getUserPayments(User user) {
        return paymentRepository.findByUserOrderByPaymentDateDesc(user);
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        return paymentOpt.orElse(null);
    }

    @Override
    @Transactional
    public Payment updatePaymentStatus(Long paymentId, String statusStr) {
        Optional<Payment> paymentOpt = paymentRepository.findById(paymentId);
        if (paymentOpt.isPresent()) {
            Payment payment = paymentOpt.get();
            
            // Convert string status to enum
            PaymentStatus status = PaymentStatus.getByCode(statusStr);
            if (status == null) {
                status = PaymentStatus.PENDING;
            }
            
            payment.setStatus(status);
            
            // Log the payment status change
            transactionLoggerService.logPaymentTransaction(
                payment,
                payment.getUser(),
                "PAYMENT_STATUS_UPDATED",
                "Payment status updated to " + status.getDescription(),
                null
            );
            
            return paymentRepository.save(payment);
        }
        return null;
    }

    @Override
    public BigDecimal getTotalPaidByUser(User user) {
        List<Payment> payments = paymentRepository.findByUserAndStatus(user, PaymentStatus.COMPLETED);
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getRemainingFineAmount(User user) {
        return user.getAccount().getFineAmount();
    }
    
    @Override
    @Transactional
    public Payment processSubscriptionPayment(User user, BigDecimal amount, String paymentMethodStr, Subscription subscription) {
        // Convert string payment method to enum
        PaymentMethod paymentMethod = PaymentMethod.getByCode(paymentMethodStr);
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.OTHER;
        }
        
        // Create a new payment for subscription
        Payment payment = new Payment(user, amount, paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setDescription("Abonnement " + subscription.getSubscriptionType().getDisplayName());
        payment.setPaymentType(PaymentType.SUBSCRIPTION);
        payment.setSubscription(subscription);
        
        // Generate a transaction ID
        String transactionId = "SUB" + System.currentTimeMillis();
        payment.setTransactionId(transactionId);
        
        // Save the payment
        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional
    public Payment processSubscriptionChangePayment(User user, BigDecimal amount, String paymentMethodStr,
                                                  Subscription subscription, SubscriptionType oldSubscriptionType,
                                                  boolean isUpgrade) {
        // Convert string payment method to enum
        PaymentMethod paymentMethod = PaymentMethod.getByCode(paymentMethodStr);
        if (paymentMethod == null) {
            paymentMethod = PaymentMethod.OTHER;
        }
        
        // Create a new payment for subscription change
        Payment payment = new Payment(user, amount, paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setStatus(PaymentStatus.COMPLETED);
        
        String changeType = isUpgrade ? "Amélioration" : "Rétrogradation";
        payment.setDescription(changeType + " d'abonnement: " +
                              oldSubscriptionType.getDisplayName() + " vers " +
                              subscription.getSubscriptionType().getDisplayName());
        
        payment.setPaymentType(PaymentType.SUBSCRIPTION);
        payment.setSubscription(subscription);
        
        // Generate a transaction ID
        String transactionId = "CHG" + System.currentTimeMillis();
        payment.setTransactionId(transactionId);
        
        // Save the payment
        return paymentRepository.save(payment);
    }
    
    @Override
    @Transactional
    public Payment processSubscriptionRefund(User user, BigDecimal amount, Subscription subscription,
                                           SubscriptionType oldSubscriptionType) {
        // Create a new refund record
        Payment refund = new Payment(user, amount, PaymentMethod.ACCOUNT_BALANCE);
        refund.setPaymentDate(LocalDateTime.now());
        refund.setStatus(PaymentStatus.COMPLETED);
        refund.setDescription("Remboursement partiel pour changement d'abonnement: " +
                             oldSubscriptionType.getDisplayName() + " vers " +
                             subscription.getSubscriptionType().getDisplayName());
        
        refund.setPaymentType(PaymentType.REFUND);
        refund.setSubscription(subscription);
        refund.setIsRefund(true);
        
        // Generate a transaction ID
        String transactionId = "REF" + System.currentTimeMillis();
        refund.setTransactionId(transactionId);
        
        // Save the refund record
        return paymentRepository.save(refund);
    }
    
    @Override
    public List<Payment> getUserSubscriptionPayments(User user) {
        List<PaymentType> subscriptionPaymentTypes = List.of(PaymentType.SUBSCRIPTION, PaymentType.REFUND);
        return paymentRepository.findByUserAndPaymentTypeInOrderByPaymentDateDesc(user, subscriptionPaymentTypes);
    }
}
package com.library.service;

import java.math.BigDecimal;
import java.util.List;

import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import com.library.enums.SubscriptionType;
import com.library.model.Payment;
import com.library.model.Subscription;
import com.library.model.User;

public interface PaymentService {
    
    /**
     * Process a payment for a user's fine
     *
     * @param user The user making the payment
     * @param amount The amount to pay
     * @param paymentMethod The payment method used
     * @return The created payment record
     */
    Payment processPayment(User user, BigDecimal amount, String paymentMethodStr);
    
    /**
     * Process a payment for a user's fine with additional details
     *
     * @param user The user making the payment
     * @param amount The amount to pay
     * @param paymentMethod The payment method used
     * @param description Description of the payment
     * @param billingAddress Billing address for the payment
     * @return The created payment record
     */
    Payment processPayment(User user, BigDecimal amount, String paymentMethodStr, String description, String billingAddress);
    
    /**
     * Process a payment for a subscription purchase
     *
     * @param user The user making the payment
     * @param amount The amount to pay
     * @param paymentMethod The payment method used
     * @param subscription The subscription being purchased
     * @return The created payment record
     */
    Payment processSubscriptionPayment(User user, BigDecimal amount, String paymentMethodStr, Subscription subscription);
    
    /**
     * Process a payment for a subscription upgrade/downgrade
     *
     * @param user The user making the payment
     * @param amount The amount to pay
     * @param paymentMethod The payment method used
     * @param subscription The new subscription
     * @param oldSubscriptionType The previous subscription type
     * @param isUpgrade Whether this is an upgrade or downgrade
     * @return The created payment record
     */
    Payment processSubscriptionChangePayment(User user, BigDecimal amount, String paymentMethodStr,
                                           Subscription subscription, SubscriptionType oldSubscriptionType,
                                           boolean isUpgrade);
    
    /**
     * Process a refund for a subscription change
     *
     * @param user The user receiving the refund
     * @param amount The amount to refund
     * @param subscription The subscription being changed
     * @param oldSubscriptionType The previous subscription type
     * @return The created refund record
     */
    Payment processSubscriptionRefund(User user, BigDecimal amount, Subscription subscription,
                                    SubscriptionType oldSubscriptionType);
    
    /**
     * Get all payments for a user
     *
     * @param user The user to get payments for
     * @return A list of payments
     */
    List<Payment> getUserPayments(User user);
    
    /**
     * Get all subscription payments for a user
     *
     * @param user The user to get subscription payments for
     * @return A list of subscription payments
     */
    List<Payment> getUserSubscriptionPayments(User user);
    
    /**
     * Get a payment by ID
     *
     * @param paymentId The payment ID
     * @return The payment, or null if not found
     */
    Payment getPaymentById(Long paymentId);
    
    /**
     * Update a payment's status
     *
     * @param paymentId The payment ID
     * @param status The new status
     * @return The updated payment
     */
    Payment updatePaymentStatus(Long paymentId, String statusStr);
    
    /**
     * Get the total amount paid by a user
     *
     * @param user The user
     * @return The total amount paid
     */
    BigDecimal getTotalPaidByUser(User user);
    
    /**
     * Get the remaining fine amount for a user
     *
     * @param user The user
     * @return The remaining fine amount
     */
    BigDecimal getRemainingFineAmount(User user);
}
package com.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import com.library.enums.PaymentType;
import com.library.model.Payment;
import com.library.model.Subscription;
import com.library.model.User;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    List<Payment> findByUser(User user);
    
    List<Payment> findByUserOrderByPaymentDateDesc(User user);
    
    List<Payment> findByStatus(PaymentStatus status);
    
    List<Payment> findByUserAndStatus(User user, PaymentStatus status);
    
    List<Payment> findByPaymentType(PaymentType paymentType);
    
    List<Payment> findByUserAndPaymentType(User user, PaymentType paymentType);
    
    List<Payment> findByUserAndPaymentTypeOrderByPaymentDateDesc(User user, PaymentType paymentType);
    
    List<Payment> findBySubscription(Subscription subscription);
    
    List<Payment> findByUserAndPaymentTypeIn(User user, List<PaymentType> paymentTypes);
    
    List<Payment> findByUserAndPaymentTypeInOrderByPaymentDateDesc(User user, List<PaymentType> paymentTypes);
    
    List<Payment> findByPaymentMethod(PaymentMethod paymentMethod);
    
    List<Payment> findByUserAndPaymentMethod(User user, PaymentMethod paymentMethod);
}
package com.library.service.impl;

import com.library.model.Payment;
import com.library.model.TransactionLog;
import com.library.model.User;
import com.library.repository.TransactionLogRepository;
import com.library.service.TransactionLoggerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of the TransactionLoggerService interface.
 */
@Service
public class TransactionLoggerServiceImpl implements TransactionLoggerService {

    @Autowired
    private TransactionLogRepository transactionLogRepository;

    @Override
    public TransactionLog logPaymentTransaction(Payment payment, User user, String action, String details, String ipAddress) {
        TransactionLog log = new TransactionLog();
        log.setPayment(payment);
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());
        log.setIsSecurityEvent(false);
        
        // Set payment-related information if available
        if (payment != null) {
            log.setAmount(payment.getAmount());
            log.setPaymentMethod(payment.getPaymentMethod());
            log.setPaymentType(payment.getPaymentType());
            log.setPaymentStatus(payment.getStatus());
        }
        
        return transactionLogRepository.save(log);
    }

    @Override
    public TransactionLog logSecurityEvent(User user, String action, String details, String ipAddress) {
        TransactionLog log = new TransactionLog();
        log.setUser(user);
        log.setAction(action);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());
        log.setIsSecurityEvent(true);
        
        return transactionLogRepository.save(log);
    }

    @Override
    public List<TransactionLog> getTransactionLogsForPayment(Payment payment) {
        return transactionLogRepository.findByPaymentOrderByTimestampDesc(payment);
    }

    @Override
    public List<TransactionLog> getTransactionLogsForUser(User user) {
        return transactionLogRepository.findByUserOrderByTimestampDesc(user);
    }

    @Override
    public List<TransactionLog> getSecurityEventsForUser(User user) {
        return transactionLogRepository.findByUserAndIsSecurityEventOrderByTimestampDesc(user, true);
    }

    @Override
    public TransactionLog getTransactionLogById(Long id) {
        Optional<TransactionLog> log = transactionLogRepository.findById(id);
        return log.orElse(null);
    }

    @Override
    public void deleteTransactionLog(Long id) {
        transactionLogRepository.deleteById(id);
    }
}
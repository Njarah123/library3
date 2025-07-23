package com.library.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.enums.PaymentMethod;
import com.library.enums.PaymentStatus;
import com.library.enums.PaymentType;
import com.library.model.Payment;
import com.library.model.User;
import com.library.service.PaymentService;
import com.library.service.SubscriptionService;
import com.library.service.TransactionLoggerService;
import com.library.service.UserService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private TransactionLoggerService transactionLoggerService;
    
    /**
     * Display the payment form
     */
    @GetMapping
    public String showPaymentForm(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        BigDecimal fineAmount = user.getAccount().getFineAmount();
        
        model.addAttribute("user", user);
        model.addAttribute("fineAmount", fineAmount);
        
        return "payments/payment-form";
    }
    
    /**
     * Process a payment
     */
    @PostMapping("/process")
    public String processPayment(
            @RequestParam("amount") BigDecimal amount,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam(value = "billingAddress", required = false) String billingAddress,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(principal.getName());
        BigDecimal fineAmount = user.getAccount().getFineAmount();
        
        // Validate payment amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            redirectAttributes.addFlashAttribute("error", "Le montant du paiement doit être supérieur à zéro.");
            return "redirect:/payments";
        }
        
        if (amount.compareTo(fineAmount) > 0) {
            redirectAttributes.addFlashAttribute("error", "Le montant du paiement ne peut pas être supérieur au montant de l'amende.");
            return "redirect:/payments";
        }
        
        // Set default billing address if not provided
        if (billingAddress == null || billingAddress.trim().isEmpty()) {
            billingAddress = user.getEmail() != null ? user.getEmail() : "Non spécifiée";
        }
        
        // Process the payment
        String description = "Paiement d'amende";
        Payment payment = paymentService.processPayment(user, amount, paymentMethod, description, billingAddress);
        
        redirectAttributes.addFlashAttribute("success", "Paiement de " + amount + "€ traité avec succès.");
        
        // Redirect to receipt page
        return "redirect:/payments/receipt/" + payment.getId();
    }
    
    /**
     * Display payment history
     */
    @GetMapping("/history")
    public String showPaymentHistory(
            @RequestParam(value = "type", required = false) String paymentType,
            @RequestParam(value = "method", required = false) String paymentMethod,
            @RequestParam(value = "status", required = false) String status,
            Model model, Principal principal) {
        
        User user = userService.findByUsername(principal.getName());
        
        // Get all payments
        List<Payment> allPayments = paymentService.getUserPayments(user);
        
        // Get subscription payments
        List<Payment> subscriptionPayments = paymentService.getUserSubscriptionPayments(user);
        
        // Filter payments based on query parameters
        List<Payment> filteredPayments = allPayments;
        
        if (paymentType != null && !paymentType.isEmpty()) {
            PaymentType type = PaymentType.getByCode(paymentType);
            if (type != null) {
                filteredPayments = filteredPayments.stream()
                        .filter(p -> type.equals(p.getPaymentType()))
                        .collect(Collectors.toList());
            }
        }
        
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            PaymentMethod method = PaymentMethod.getByCode(paymentMethod);
            if (method != null) {
                filteredPayments = filteredPayments.stream()
                        .filter(p -> method.equals(p.getPaymentMethod()))
                        .collect(Collectors.toList());
            }
        }
        
        if (status != null && !status.isEmpty()) {
            PaymentStatus paymentStatus = PaymentStatus.getByCode(status);
            if (paymentStatus != null) {
                filteredPayments = filteredPayments.stream()
                        .filter(p -> paymentStatus.equals(p.getStatus()))
                        .collect(Collectors.toList());
            }
        }
        
        // Calculate totals
        BigDecimal totalPaid = paymentService.getTotalPaidByUser(user);
        BigDecimal fineAmount = user.getAccount().getFineAmount();
        
        // Get payment methods and types for filtering
        Map<String, String> paymentMethods = new HashMap<>();
        for (PaymentMethod method : PaymentMethod.values()) {
            paymentMethods.put(method.getCode(), method.getDescription());
        }
        
        Map<String, String> paymentTypes = new HashMap<>();
        for (PaymentType type : PaymentType.values()) {
            paymentTypes.put(type.getCode(), type.getDescription());
        }
        
        Map<String, String> paymentStatuses = new HashMap<>();
        for (PaymentStatus paymentStatus : PaymentStatus.values()) {
            paymentStatuses.put(paymentStatus.getCode(), paymentStatus.getDescription());
        }
        
        // Add attributes to model
        model.addAttribute("user", user);
        model.addAttribute("payments", filteredPayments);
        model.addAttribute("subscriptionPayments", subscriptionPayments);
        model.addAttribute("totalPaid", totalPaid);
        model.addAttribute("fineAmount", fineAmount);
        model.addAttribute("paymentMethods", paymentMethods);
        model.addAttribute("paymentTypes", paymentTypes);
        model.addAttribute("paymentService", paymentService);
        model.addAttribute("paymentStatuses", paymentStatuses);
        model.addAttribute("selectedType", paymentType);
        model.addAttribute("selectedMethod", paymentMethod);
        model.addAttribute("selectedStatus", status);
        
        return "payments/payment-history";
    }
    
    /**
     * Display payment receipt
     */
    @GetMapping("/receipt/{id}")
    public String showPaymentReceipt(@PathVariable("id") Long paymentId, Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Payment payment = paymentService.getPaymentById(paymentId);
        
        // Verify payment exists and belongs to the user
        if (payment == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Payment not found");
        }
        
        if (!payment.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
        }
        
        model.addAttribute("payment", payment);
        
        // Log the receipt view for security tracking
        transactionLoggerService.logPaymentTransaction(
            payment,
            user,
            "PAYMENT_RECEIPT_VIEWED",
            "Payment receipt viewed for payment ID: " + payment.getId(),
            null
        );
        
        return "payments/receipt";
    }
}
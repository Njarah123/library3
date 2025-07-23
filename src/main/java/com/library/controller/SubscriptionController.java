package com.library.controller;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.dto.SubscriptionDTO;
import com.library.enums.SubscriptionType;
import com.library.model.Payment;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.service.NotificationService;
import com.library.service.PaymentService;
import com.library.service.SubscriptionService;
import com.library.service.UserService;

@Controller
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final NotificationService notificationService;
    private final PaymentService paymentService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService, UserService userService,
                                 NotificationService notificationService, PaymentService paymentService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
    }
    

    /**
     * Display subscription plans page
     */
    @GetMapping("/plans")
    @PreAuthorize("hasRole('STUDENT')")
    public String showSubscriptionPlans(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        // Get all subscription types
        List<SubscriptionType> subscriptionTypes = Arrays.asList(SubscriptionType.values());
        
        // Get user's active subscription if exists
        Optional<Subscription> activeSubscription = subscriptionService.getActiveSubscription(user);
        
        model.addAttribute("subscriptionTypes", subscriptionTypes);
        model.addAttribute("user", user);
        
        if (activeSubscription.isPresent()) {
            model.addAttribute("activeSubscription",
                    SubscriptionDTO.fromSubscription(activeSubscription.get()));
        }
        
        return "subscriptions/plans";
    }

    /**
     * Display subscription checkout page
     */
    @GetMapping("/checkout/{type}")
    @PreAuthorize("hasRole('STUDENT')")
    public String showCheckout(@PathVariable("type") String subscriptionTypeStr,
                              @RequestParam(required = false) Long bookId,
                              Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        try {
            SubscriptionType subscriptionType = SubscriptionType.valueOf(subscriptionTypeStr.toUpperCase());
            model.addAttribute("subscriptionType", subscriptionType);
            model.addAttribute("user", user);
            
            // If bookId is provided, store it for redirect after purchase
            if (bookId != null) {
                model.addAttribute("bookId", bookId);
            }
            
            return "subscriptions/checkout";
        } catch (IllegalArgumentException e) {
            return "redirect:/subscriptions/plans";
        }
    }

    /**
     * Process subscription purchase
     */
    @PostMapping("/purchase")
    @PreAuthorize("hasRole('STUDENT')")
    public String purchaseSubscription(@RequestParam("type") String subscriptionTypeStr,
                                      @RequestParam(required = false) Long bookId,
                                      @RequestParam(value = "paymentMethod", defaultValue = "balance") String paymentMethod,
                                      RedirectAttributes redirectAttributes,
                                      Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        try {
            SubscriptionType subscriptionType = SubscriptionType.valueOf(subscriptionTypeStr.toUpperCase());
            
            // Check if user has enough balance when using account balance
            if (paymentMethod.equals("balance") && user.getBalance() < subscriptionType.getPrice()) {
                redirectAttributes.addFlashAttribute("error",
                        "Insufficient balance. Please add funds to your account.");
                return "redirect:/subscriptions/checkout/" + subscriptionTypeStr +
                       (bookId != null ? "?bookId=" + bookId : "");
            }
            
            // Create the subscription
            Subscription subscription = subscriptionService.createSubscription(user, subscriptionType);
            
            // Process payment based on payment method
            if (paymentMethod.equals("balance")) {
                // Deduct the subscription cost from user's balance
                user.setBalance(user.getBalance() - subscriptionType.getPrice());
                userService.save(user);
            }
            
            // Create payment record
            BigDecimal amount = BigDecimal.valueOf(subscriptionType.getPrice());
            paymentService.processSubscriptionPayment(user, amount, paymentMethod, subscription);
            
            // Add success message
            redirectAttributes.addFlashAttribute("success",
                    "Successfully purchased " + subscriptionType.getDisplayName() + " subscription!");
            
            // Create notification
            notificationService.createSubscriptionPurchaseNotification(user, subscriptionType);
            
            // If bookId is provided, redirect to borrow attempt page
            // which will show the confirmation page
            if (bookId != null) {
                return "redirect:/borrow-attempt/" + bookId;
            }
            
            return "redirect:/student/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Invalid subscription type.");
            return "redirect:/subscriptions/plans";
        }
    }

    /**
     * Get user's active subscription
     */
    @GetMapping("/active")
    @ResponseBody
    public ResponseEntity<?> getActiveSubscription(Principal principal) {
        User user = userService.findByUsername(principal.getName());
        Optional<Subscription> subscription = subscriptionService.getActiveSubscription(user);
        
        if (subscription.isPresent()) {
            return ResponseEntity.ok(SubscriptionDTO.fromSubscription(subscription.get()));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No active subscription found");
        }
    }

    /**
     * Cancel user's active subscription
     */
    @PostMapping("/cancel")
    public String cancelSubscription(RedirectAttributes redirectAttributes, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        boolean cancelled = subscriptionService.cancelSubscription(user);
        
        if (cancelled) {
            redirectAttributes.addFlashAttribute("success", "Subscription cancelled successfully.");
            notificationService.createSubscriptionCancellationNotification(user);
        } else {
            redirectAttributes.addFlashAttribute("error", "No active subscription to cancel.");
        }
        
        return "redirect:/subscriptions/plans";
    }

    /**
     * Get all subscriptions for admin/librarian
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('LIBRARIAN')")
    @ResponseBody
    public List<SubscriptionDTO> getAllSubscriptions() {
        List<Subscription> subscriptions = subscriptionService.getAllActiveSubscriptions();
        return subscriptions.stream()
                .map(SubscriptionDTO::fromSubscription)
                .collect(Collectors.toList());
    }
    
    /**
     * Display subscription renewal page
     */
    @GetMapping("/student/subscription/renew")
    @PreAuthorize("hasRole('STUDENT')")
    public String showRenewalPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        // Get all subscription types
        List<SubscriptionType> subscriptionTypes = Arrays.asList(SubscriptionType.values());
        
        // Get user's active subscription
        Optional<Subscription> activeSubscription = subscriptionService.getActiveSubscription(user);
        
        if (activeSubscription.isEmpty()) {
            // Redirect to plans page if no active subscription
            return "redirect:/subscriptions/plans";
        }
        
        model.addAttribute("subscriptionTypes", subscriptionTypes);
        model.addAttribute("user", user);
        model.addAttribute("currentSubscription",
                SubscriptionDTO.fromSubscription(activeSubscription.get()));
        
        return "student/subscription-renew";
    }
    
    /**
     * Process subscription renewal
     */
    @PostMapping("/student/subscription/process-renewal")
    @PreAuthorize("hasRole('STUDENT')")
    public String processRenewal(@RequestParam("subscriptionType") String subscriptionTypeStr,
                               @RequestParam("paymentMethod") String paymentMethod,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        try {
            SubscriptionType subscriptionType = SubscriptionType.valueOf(subscriptionTypeStr.toUpperCase());
            BigDecimal amount = BigDecimal.valueOf(subscriptionType.getPrice());
            
            // Check if user has enough balance when using account balance
            if (paymentMethod.equals("balance") && user.getBalance() < subscriptionType.getPrice()) {
                redirectAttributes.addFlashAttribute("error",
                        "Solde insuffisant. Veuillez ajouter des fonds à votre compte.");
                return "redirect:/student/subscription/renew";
            }
            
            // Get current subscription
            Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
            if (currentSubscriptionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Aucun abonnement actif trouvé.");
                return "redirect:/subscriptions/plans";
            }
            
            Subscription currentSubscription = currentSubscriptionOpt.get();
            Subscription subscription;
            
            // If subscription type is changing, create a new subscription
            if (currentSubscription.getSubscriptionType() != subscriptionType) {
                subscription = subscriptionService.createSubscription(user, subscriptionType);
            } else {
                // Otherwise extend the current subscription
                boolean extended = subscriptionService.extendSubscription(user, subscriptionType.getValidityDays());
                if (extended) {
                    // Refresh the subscription after extension
                    Optional<Subscription> refreshedSubscription = subscriptionService.getActiveSubscription(user);
                    subscription = refreshedSubscription.orElse(currentSubscription);
                } else {
                    subscription = currentSubscription;
                }
            }
            
            // Process payment based on payment method
            if (paymentMethod.equals("balance")) {
                // Deduct the subscription cost from user's balance
                user.setBalance(user.getBalance() - subscriptionType.getPrice());
                userService.save(user);
            }
            
            // Create payment record
            paymentService.processSubscriptionPayment(user, amount, paymentMethod, subscription);
            
            // Add success message
            redirectAttributes.addFlashAttribute("success",
                    "Abonnement " + subscriptionType.getDisplayName() + " renouvelé avec succès!");
            
            // Create notification
            notificationService.createSubscriptionRenewalNotification(user, subscriptionType);
            
            return "redirect:/student/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Type d'abonnement invalide.");
            return "redirect:/student/subscription/renew";
        }
    }
    /**
     * Display subscription upgrade page
     */
    @GetMapping("/student/subscription/upgrade")
    @PreAuthorize("hasRole('STUDENT')")
    public String showUpgradePage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        // Get all subscription types
        List<SubscriptionType> subscriptionTypes = Arrays.asList(SubscriptionType.values());
        
        // Get user's active subscription
        Optional<Subscription> activeSubscription = subscriptionService.getActiveSubscription(user);
        
        if (activeSubscription.isEmpty()) {
            // Redirect to plans page if no active subscription
            return "redirect:/subscriptions/plans";
        }
        
        model.addAttribute("subscriptionTypes", subscriptionTypes);
        model.addAttribute("user", user);
        model.addAttribute("currentSubscription",
                SubscriptionDTO.fromSubscription(activeSubscription.get()));
        
        return "student/subscription-upgrade";
    }
    
    /**
     * Process subscription upgrade/downgrade
     */
    @PostMapping("/student/subscription/upgrade")
    @PreAuthorize("hasRole('STUDENT')")
    public String processUpgrade(@RequestParam("subscriptionType") String subscriptionTypeStr,
                               @RequestParam("paymentMethod") String paymentMethod,
                               RedirectAttributes redirectAttributes,
                               Principal principal) {
        User user = userService.findByUsername(principal.getName());
        
        try {
            SubscriptionType newSubscriptionType = SubscriptionType.valueOf(subscriptionTypeStr.toUpperCase());
            
            // Get current subscription
            Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
            if (currentSubscriptionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Aucun abonnement actif trouvé.");
                return "redirect:/subscriptions/plans";
            }
            
            Subscription currentSubscription = currentSubscriptionOpt.get();
            SubscriptionType currentType = currentSubscription.getSubscriptionType();
            
            // If same subscription type, redirect to renewal page
            if (currentType == newSubscriptionType) {
                redirectAttributes.addFlashAttribute("info",
                        "Vous avez déjà un abonnement " + currentType.getDisplayName() + ". Utilisez l'option de renouvellement.");
                return "redirect:/student/subscription/renew";
            }
            
            // Calculate price difference based on remaining days
            double currentPrice = currentType.getPrice();
            double newPrice = newSubscriptionType.getPrice();
            long daysRemaining = currentSubscription.getEndDate().toLocalDate().toEpochDay() -
                                 java.time.LocalDate.now().toEpochDay();
            
            // Calculate prorated refund for current subscription
            double daysTotal = currentType.getValidityDays();
            double refundAmount = (daysRemaining / daysTotal) * currentPrice;
            
            // Calculate final price (new subscription price - refund)
            double finalPrice = newPrice - refundAmount;
            if (finalPrice < 0) finalPrice = 0; // Ensure price is not negative
            
            // Check if user has enough balance when using account balance
            if (paymentMethod.equals("balance") && user.getBalance() < finalPrice) {
                redirectAttributes.addFlashAttribute("error",
                        "Solde insuffisant. Veuillez ajouter des fonds à votre compte ou choisir une autre méthode de paiement.");
                return "redirect:/student/subscription/upgrade";
            }
            
            // Create the new subscription (this will automatically cancel the current one)
            Subscription newSubscription = subscriptionService.createSubscription(user, newSubscriptionType);
            
            // Determine if this is an upgrade or downgrade
            boolean isUpgrade = newPrice > currentPrice;
            String changeType = isUpgrade ? "amélioré" : "rétrogradé";
            
            // Process payment and refund
            if (refundAmount > 0) {
                // Create refund record for the prorated amount of the old subscription
                paymentService.processSubscriptionRefund(
                    user,
                    BigDecimal.valueOf(refundAmount),
                    newSubscription,
                    currentType
                );
            }
            
            if (finalPrice > 0) {
                // Process payment based on payment method
                if (paymentMethod.equals("balance")) {
                    // Deduct from user's balance
                    user.setBalance(user.getBalance() - finalPrice);
                    userService.save(user);
                }
                
                // Create payment record for the new subscription
                paymentService.processSubscriptionChangePayment(
                    user,
                    BigDecimal.valueOf(finalPrice),
                    paymentMethod,
                    newSubscription,
                    currentType,
                    isUpgrade
                );
            }
            
            // Add success message
            redirectAttributes.addFlashAttribute("success",
                    "Votre abonnement a été " + changeType + " avec succès vers " +
                    newSubscriptionType.getDisplayName() + "!");
            
            // Create notification
            notificationService.createSubscriptionChangeNotification(user, currentType, newSubscriptionType);
            
            return "redirect:/student/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Type d'abonnement invalide.");
            return "redirect:/student/subscription/upgrade";
        }
    }
    
    /**
     * Display subscription history page
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    public String showSubscriptionHistory(
            @RequestParam(value = "type", required = false) String subscriptionType,
            @RequestParam(value = "status", required = false) String status,
            Model model, Principal principal) {
        
        User user = userService.findByUsername(principal.getName());
        
        // Get all subscriptions for the user
        List<Subscription> allSubscriptions = subscriptionService.getAllSubscriptions(user);
        
        // Get all subscription payments
        List<Payment> subscriptionPayments = paymentService.getUserSubscriptionPayments(user);
        
        // Filter subscriptions based on query parameters
        List<Subscription> filteredSubscriptions = allSubscriptions;
        
        if (subscriptionType != null && !subscriptionType.isEmpty()) {
            try {
                SubscriptionType type = SubscriptionType.valueOf(subscriptionType.toUpperCase());
                filteredSubscriptions = filteredSubscriptions.stream()
                        .filter(s -> s.getSubscriptionType() == type)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid subscription type, ignore filter
            }
        }
        
        if (status != null && !status.isEmpty()) {
            boolean isActive = "active".equalsIgnoreCase(status);
            filteredSubscriptions = filteredSubscriptions.stream()
                    .filter(s -> s.isActive() == isActive)
                    .collect(Collectors.toList());
        }
        
        // Get all subscription types for filtering
        List<SubscriptionType> subscriptionTypes = Arrays.asList(SubscriptionType.values());
        
        // Get current active subscription
        Optional<Subscription> activeSubscription = subscriptionService.getActiveSubscription(user);
        
        // Add attributes to model
        model.addAttribute("user", user);
        model.addAttribute("subscriptions", filteredSubscriptions);
        model.addAttribute("subscriptionPayments", subscriptionPayments);
        model.addAttribute("subscriptionTypes", subscriptionTypes);
        model.addAttribute("selectedType", subscriptionType);
        model.addAttribute("selectedStatus", status);
        
        if (activeSubscription.isPresent()) {
            model.addAttribute("activeSubscription",
                    SubscriptionDTO.fromSubscription(activeSubscription.get()));
        }
        
        return "subscriptions/subscription-history";
    }
}
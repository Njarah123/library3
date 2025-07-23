package com.library.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.dto.SubscriptionDTO;
import com.library.enums.SubscriptionType;
import com.library.model.Borrowing;
import com.library.model.Payment;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.repository.BorrowingRepository;
import com.library.service.BorrowingService;
import com.library.service.NotificationService;
import com.library.service.PaymentService;
import com.library.service.SubscriptionService;
import com.library.service.UserService;

@Controller
@RequestMapping("/student")
public class StudentSubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @Autowired
    private UserService userService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private BorrowingService borrowingService;
    
    @Autowired
    private BorrowingRepository borrowingRepository;
    
    @Autowired
    private NotificationService notificationService;

    /**
     * Redirect to subscription plans page from student dashboard
     */
    @GetMapping("/subscriptions")
    @PreAuthorize("hasRole('STUDENT')")
    public String redirectToPlans() {
        return "redirect:/subscriptions/plans";
    }

    /**
     * Display the subscription management dashboard for students
     */
    @GetMapping("/subscription/dashboard")
    @PreAuthorize("hasRole('STUDENT')")
    public String showSubscriptionDashboard(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        
        // Ajouter le nombre de notifications non lues
        long unreadNotifications = notificationService.getUnreadNotificationCount(user);
        model.addAttribute("unreadNotifications", unreadNotifications);
        
        // Ajouter les compteurs pour la barre latérale
        model.addAttribute("newBooksCount", 0); // Remplacer par la valeur réelle si disponible
        model.addAttribute("pendingBorrowsCount", 0); // Remplacer par la valeur réelle si disponible

        // Get current subscription
        Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
        if (currentSubscriptionOpt.isPresent()) {
            Subscription currentSubscription = currentSubscriptionOpt.get();
            SubscriptionDTO subscriptionDTO = SubscriptionDTO.fromSubscription(currentSubscription);
            model.addAttribute("currentSubscription", subscriptionDTO);
        }

        // Get subscription history
        List<Subscription> subscriptionHistory = subscriptionService.getAllSubscriptions(user);
        List<SubscriptionDTO> subscriptionDTOs = subscriptionHistory.stream()
                .map(SubscriptionDTO::fromSubscription)
                .collect(Collectors.toList());
        model.addAttribute("subscriptionHistory", subscriptionDTOs);

        // Get payment history
        List<Payment> paymentHistory = paymentService.getUserPayments(user);
        model.addAttribute("paymentHistory", paymentHistory);

        // Get borrowing statistics
        List<Borrowing> borrowings = borrowingService.getBorrowingHistory(user);
        int borrowedBooks = borrowings.size();
        int returnedBooks = (int) borrowings.stream()
                .filter(b -> b.getReturnDate() != null)
                .count();
        int currentlyBorrowed = borrowedBooks - returnedBooks;
        int totalSubscriptions = subscriptionHistory.size();

        model.addAttribute("borrowedBooks", borrowedBooks);
        model.addAttribute("returnedBooks", returnedBooks);
        model.addAttribute("currentlyBorrowed", currentlyBorrowed);
        model.addAttribute("totalSubscriptions", totalSubscriptions);
        
        // Ajouter les statistiques quotidiennes d'emprunt pour le graphique (données réelles des 7 derniers jours)
        List<Integer> dailyBorrowStats = getDailyActivityData(user);
        model.addAttribute("dailyBorrowStats", dailyBorrowStats);
        
        // Add subscription types for upgrade plans
        model.addAttribute("standardPlan", SubscriptionType.STANDARD);
        model.addAttribute("premiumPlan", SubscriptionType.PREMIUM);

        return "student/subscription-dashboard";
    }
    
    /**
     * Display the subscription renewal page
     */
    @GetMapping("/subscription/renew")
    @PreAuthorize("hasRole('STUDENT')")
    public String showRenewalPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        
        // Ajouter les compteurs pour la barre latérale
        model.addAttribute("newBooksCount", 0);
        model.addAttribute("pendingBorrowsCount", 0);

        // Get current subscription
        Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
        if (currentSubscriptionOpt.isPresent()) {
            Subscription currentSubscription = currentSubscriptionOpt.get();
            SubscriptionDTO subscriptionDTO = SubscriptionDTO.fromSubscription(currentSubscription);
            model.addAttribute("currentSubscription", subscriptionDTO);
        } else {
            // Redirect to subscription plans if no active subscription
            return "redirect:/subscriptions/plans";
        }

        return "student/subscription-renew";
    }
    
    /**
     * Process subscription renewal
     */
    @PostMapping("/subscription/renew")
    @PreAuthorize("hasRole('STUDENT')")
    public String processRenewal(
            @RequestParam("subscriptionType") String subscriptionTypeStr,
            @RequestParam("paymentMethod") String paymentMethod,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(principal.getName());
        boolean success = false;
        
        try {
            // Convert string to enum
            SubscriptionType selectedType = SubscriptionType.valueOf(subscriptionTypeStr);
            
            // Get current subscription
            Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
            
            if (currentSubscriptionOpt.isPresent()) {
                Subscription currentSubscription = currentSubscriptionOpt.get();
                
                // If same subscription type, extend by the validity days of that type
                if (currentSubscription.getSubscriptionType() == selectedType) {
                    int daysToAdd = selectedType.getValidityDays();
                    success = subscriptionService.extendSubscription(user, daysToAdd);
                } else {
                    // If different subscription type, create a new subscription
                    Subscription newSubscription = subscriptionService.createSubscription(user, selectedType);
                    success = newSubscription != null;
                }
                
                // TODO: Process payment based on paymentMethod
                // This would be implemented in a real payment processing system
                System.out.println("Payment method selected: " + paymentMethod);
            }
            
            if (success) {
                redirectAttributes.addFlashAttribute("message", "Votre abonnement a été renouvelé avec succès.");
                redirectAttributes.addFlashAttribute("messageType", "success");
            } else {
                redirectAttributes.addFlashAttribute("message", "Impossible de renouveler votre abonnement. Veuillez réessayer.");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "Type d'abonnement invalide. Veuillez réessayer.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Une erreur est survenue. Veuillez réessayer.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/student/subscription/dashboard";
    }
    
    /**
     * Display the subscription cancellation page
     */
    @GetMapping("/subscription/cancel")
    @PreAuthorize("hasRole('STUDENT')")
    public String showCancellationPage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        
        // Ajouter les compteurs pour la barre latérale
        model.addAttribute("newBooksCount", 0);
        model.addAttribute("pendingBorrowsCount", 0);

        // Get current subscription
        Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
        if (currentSubscriptionOpt.isPresent()) {
            Subscription currentSubscription = currentSubscriptionOpt.get();
            SubscriptionDTO subscriptionDTO = SubscriptionDTO.fromSubscription(currentSubscription);
            model.addAttribute("currentSubscription", subscriptionDTO);
            
            // Get current borrowings
            List<Borrowing> activeBorrowings = borrowingService.getCurrentBorrowings(user);
            model.addAttribute("activeBorrowings", activeBorrowings);
            model.addAttribute("activeBorrowingsCount", activeBorrowings.size());
        } else {
            // Redirect to subscription plans if no active subscription
            return "redirect:/subscriptions/plans";
        }

        return "student/subscription-cancel";
    }
    
    /**
     * Process subscription cancellation
     */
    @PostMapping("/subscription/cancel/confirm")
    @PreAuthorize("hasRole('STUDENT')")
    public String processCancellation(Principal principal, RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        
        // Check if user has active borrowings
        List<Borrowing> activeBorrowings = borrowingService.getCurrentBorrowings(user);
        if (!activeBorrowings.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Vous devez retourner tous les livres empruntés avant d'annuler votre abonnement.");
            redirectAttributes.addFlashAttribute("messageType", "error");
            return "redirect:/student/subscription/cancel";
        }
        
        boolean success = subscriptionService.cancelSubscription(user);
        
        if (success) {
            redirectAttributes.addFlashAttribute("message", "Votre abonnement a été annulé avec succès.");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } else {
            redirectAttributes.addFlashAttribute("message", "Impossible d'annuler votre abonnement. Veuillez réessayer.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/student/dashboard";
    }
    
    /**
     * Display the subscription upgrade page
     */
    @GetMapping("/subscription/upgrade")
    @PreAuthorize("hasRole('STUDENT')")
    public String showUpgradePage(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName());
        model.addAttribute("user", user);
        
        // Ajouter les compteurs pour la barre latérale
        model.addAttribute("newBooksCount", 0);
        model.addAttribute("pendingBorrowsCount", 0);

        // Get current subscription
        Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
        if (currentSubscriptionOpt.isPresent()) {
            Subscription currentSubscription = currentSubscriptionOpt.get();
            SubscriptionDTO subscriptionDTO = SubscriptionDTO.fromSubscription(currentSubscription);
            model.addAttribute("currentSubscription", subscriptionDTO);
        } else {
            // Redirect to subscription plans if no active subscription
            return "redirect:/subscriptions/plans";
        }

        return "student/subscription-upgrade";
    }
    
    /**
     * Process subscription upgrade
     */
    @PostMapping("/subscription/upgrade")
    @PreAuthorize("hasRole('STUDENT')")
    public String processUpgrade(
            @RequestParam("subscriptionType") String subscriptionTypeStr,
            @RequestParam("paymentMethod") String paymentMethod,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(principal.getName());
        
        try {
            // Convert string to enum
            SubscriptionType newSubscriptionType = SubscriptionType.valueOf(subscriptionTypeStr);
            
            // Get current subscription
            Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
            
            if (currentSubscriptionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Aucun abonnement actif trouvé.");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/subscriptions/plans";
            }
            
            Subscription currentSubscription = currentSubscriptionOpt.get();
            SubscriptionType currentType = currentSubscription.getSubscriptionType();
            
            // If same subscription type, redirect to renewal page
            if (currentType == newSubscriptionType) {
                redirectAttributes.addFlashAttribute("message",
                        "Vous avez déjà un abonnement " + currentType.getDisplayName() + ". Utilisez l'option de renouvellement.");
                redirectAttributes.addFlashAttribute("messageType", "info");
                return "redirect:/student/subscription/renew";
            }
            
            // Create the new subscription (this will automatically replace the current one)
            Subscription newSubscription = subscriptionService.createSubscription(user, newSubscriptionType);
            
            // Determine if this is an upgrade or downgrade
            boolean isUpgrade = newSubscriptionType.getPrice() > currentType.getPrice();
            String changeType = isUpgrade ? "amélioré" : "rétrogradé";
            
            // TODO: Process payment based on paymentMethod
            // This would be implemented in a real payment processing system
            System.out.println("Payment method selected: " + paymentMethod);
            
            boolean success = newSubscription != null;
            
            if (success) {
                redirectAttributes.addFlashAttribute("message",
                        "Votre abonnement a été " + changeType + " avec succès vers " +
                        newSubscriptionType.getDisplayName() + "!");
                redirectAttributes.addFlashAttribute("messageType", "success");
                
                // Create notification
                notificationService.createSubscriptionChangeNotification(user, currentType, newSubscriptionType);
            } else {
                redirectAttributes.addFlashAttribute("message", "Impossible de modifier votre abonnement. Veuillez réessayer.");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "Type d'abonnement invalide. Veuillez réessayer.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Une erreur est survenue. Veuillez réessayer.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/student/subscription/dashboard";
    }
    
    /**
     * Process subscription upgrade to specific type
     */
    @GetMapping("/subscription/upgrade/{type}")
    @PreAuthorize("hasRole('STUDENT')")
    public String upgradeToSpecificType(@PathVariable("type") String subscriptionTypeStr,
                                       Principal principal,
                                       RedirectAttributes redirectAttributes) {
        User user = userService.findByUsername(principal.getName());
        
        try {
            SubscriptionType newSubscriptionType = SubscriptionType.valueOf(subscriptionTypeStr.toUpperCase());
            
            // Get current subscription
            Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscription(user);
            
            if (currentSubscriptionOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "Aucun abonnement actif trouvé.");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/subscriptions/plans";
            }
            
            Subscription currentSubscription = currentSubscriptionOpt.get();
            SubscriptionType currentType = currentSubscription.getSubscriptionType();
            
            // If same subscription type, redirect to renewal page
            if (currentType == newSubscriptionType) {
                redirectAttributes.addFlashAttribute("message",
                        "Vous avez déjà un abonnement " + currentType.getDisplayName() + ". Utilisez l'option de renouvellement.");
                redirectAttributes.addFlashAttribute("messageType", "info");
                return "redirect:/student/subscription/renew";
            }
            
            // Check if user has enough balance (assuming balance payment for simplicity)
            if (user.getBalance() < newSubscriptionType.getPrice()) {
                redirectAttributes.addFlashAttribute("message",
                        "Solde insuffisant. Veuillez ajouter des fonds à votre compte.");
                redirectAttributes.addFlashAttribute("messageType", "error");
                return "redirect:/student/subscription/upgrade";
            }
            
            // Create the new subscription
            Subscription newSubscription = subscriptionService.createSubscription(user, newSubscriptionType);
            
            if (newSubscription != null) {
                // Deduct from user's balance
                user.setBalance(user.getBalance() - newSubscriptionType.getPrice());
                userService.save(user);
                
                // Determine if this is an upgrade or downgrade
                boolean isUpgrade = newSubscriptionType.getPrice() > currentType.getPrice();
                String changeType = isUpgrade ? "amélioré" : "rétrogradé";
                
                redirectAttributes.addFlashAttribute("message",
                        "Votre abonnement a été " + changeType + " avec succès vers " +
                        newSubscriptionType.getDisplayName() + "!");
                redirectAttributes.addFlashAttribute("messageType", "success");
                
                // Create notification
                notificationService.createSubscriptionChangeNotification(user, currentType, newSubscriptionType);
            } else {
                redirectAttributes.addFlashAttribute("message", "Impossible de modifier votre abonnement. Veuillez réessayer.");
                redirectAttributes.addFlashAttribute("messageType", "error");
            }
            
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "Type d'abonnement invalide.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "Une erreur est survenue. Veuillez réessayer.");
            redirectAttributes.addFlashAttribute("messageType", "error");
        }
        
        return "redirect:/student/subscription/dashboard";
    }
    
    /**
     * Process add funds request
     */
    @PostMapping("/subscription/add-funds")
    @PreAuthorize("hasRole('STUDENT')")
    public String addFunds(
            @RequestParam("amount") double amount,
            @RequestParam("paymentMethod") String paymentMethod,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        
        User user = userService.findByUsername(principal.getName());
        
        try {
            // Validate amount
            if (amount < 0 || amount > 200000) {
                redirectAttributes.addFlashAttribute("error",
                    "Le montant doit être compris entre 0 et 200,000 Ar");
                return "redirect:/subscriptions/plans";
            }
            
            // Validate payment method
            if (!isValidPaymentMethod(paymentMethod)) {
                redirectAttributes.addFlashAttribute("error",
                    "Méthode de paiement invalide");
                return "redirect:/subscriptions/plans";
            }
            
            // Add funds to user balance
            double currentBalance = user.getBalance();
            user.setBalance(currentBalance + amount);
            userService.save(user);
            
            // Create a simple payment record for the funds addition
            // Using the existing payment service method with a generic approach
            try {
                // For now, we'll just add the funds without creating a complex payment record
                // In a real system, you would integrate with actual payment processors
                System.out.println("Funds added via " + paymentMethod + ": " + amount + " Ar for user: " + user.getUsername());
            } catch (Exception e) {
                System.err.println("Error processing payment: " + e.getMessage());
            }
            
            // Create a simple notification (using existing notification methods)
            try {
                // For now, we'll create a generic notification
                // In a real system, you would have a specific method for funds added
                System.out.println("Notification: Funds added for user " + user.getUsername());
            } catch (Exception e) {
                System.err.println("Error creating notification: " + e.getMessage());
            }
            
            redirectAttributes.addFlashAttribute("success",
                String.format("%.0f Ar ont été ajoutés à votre compte avec succès!", amount));
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Une erreur est survenue lors de l'ajout des fonds. Veuillez réessayer.");
        }
        
        return "redirect:/subscriptions/plans";
    }
    
    /**
     * Validate payment method
     */
    private boolean isValidPaymentMethod(String paymentMethod) {
        return paymentMethod != null &&
               (paymentMethod.equals("CARD") ||
                paymentMethod.equals("PAYPAL") ||
                paymentMethod.equals("BANK_TRANSFER") ||
                paymentMethod.equals("MOBILE_MONEY"));
    }
    
    /**
     * Récupère les données d'activité quotidienne pour les 7 derniers jours
     */
    private List<Integer> getDailyActivityData(User user) {
        List<Integer> dailyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);
            
            List<Borrowing> dayBorrows = borrowingRepository.findByUserAndBorrowDateBetween(
                user, dayStart, dayEnd);
            dailyData.add(dayBorrows.size());
        }
        
        return dailyData;
    }
}
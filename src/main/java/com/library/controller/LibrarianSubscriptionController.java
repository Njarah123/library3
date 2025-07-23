package com.library.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import com.library.model.Subscription;
import com.library.model.User;
import com.library.service.NotificationTriggerService;
import com.library.service.SubscriptionService;
import com.library.service.UserService;

/**
 * Controller for librarian subscription management
 */
@Controller
@RequestMapping("/librarian/subscriptions")
@PreAuthorize("hasRole('LIBRARIAN')")
public class LibrarianSubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationTriggerService notificationTriggerService;
    
    /**
     * Display the subscription management dashboard
     */
    @GetMapping
    public String showSubscriptionDashboard(Model model) {
        List<Subscription> activeSubscriptions = subscriptionService.getAllActiveSubscriptions();
        
        // Convert to DTOs for display
        List<SubscriptionDTO> subscriptionDTOs = activeSubscriptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        model.addAttribute("subscriptions", subscriptionDTOs);
        model.addAttribute("subscriptionTypes", SubscriptionType.values());
        
        // Add statistics
        model.addAttribute("totalActiveSubscriptions", activeSubscriptions.size());
        model.addAttribute("expiringSubscriptions", 
                subscriptionService.getSubscriptionsExpiringSoon(7).size());
        model.addAttribute("zeroQuotaSubscriptions", 
                subscriptionService.getSubscriptionsWithZeroBooksRemaining().size());
        
        return "librarian/subscription-dashboard";
    }
    
    /**
     * Filter subscriptions based on criteria
     */
    @GetMapping("/filter")
    public String filterSubscriptions(
            @RequestParam(required = false) SubscriptionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) Boolean hasQuota,
            Model model) {
        
        List<Subscription> allSubscriptions = subscriptionService.getAllActiveSubscriptions();
        
        // Apply filters
        List<Subscription> filteredSubscriptions = allSubscriptions.stream()
                .filter(s -> type == null || s.getSubscriptionType() == type)
                .filter(s -> startDate == null || !s.getStartDate().isBefore(startDate))
                .filter(s -> endDate == null || !s.getEndDate().isAfter(endDate))
                .filter(s -> hasQuota == null || (hasQuota && s.hasQuotaRemaining()) || (!hasQuota && !s.hasQuotaRemaining()))
                .collect(Collectors.toList());
        
        // Convert to DTOs for display
        List<SubscriptionDTO> subscriptionDTOs = filteredSubscriptions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        model.addAttribute("subscriptions", subscriptionDTOs);
        model.addAttribute("subscriptionTypes", SubscriptionType.values());
        model.addAttribute("selectedType", type);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("hasQuota", hasQuota);
        
        // Add statistics
        model.addAttribute("totalActiveSubscriptions", allSubscriptions.size());
        model.addAttribute("filteredCount", filteredSubscriptions.size());
        model.addAttribute("expiringSubscriptions", 
                subscriptionService.getSubscriptionsExpiringSoon(7).size());
        model.addAttribute("zeroQuotaSubscriptions", 
                subscriptionService.getSubscriptionsWithZeroBooksRemaining().size());
        
        return "librarian/subscription-dashboard";
    }
    
    /**
     * View details of a specific subscription
     */
    @GetMapping("/{id}")
    public String viewSubscriptionDetails(@PathVariable Long id, Model model) {
        Optional<Subscription> subscriptionOpt = subscriptionService.getSubscriptionById(id);
        
        if (subscriptionOpt.isEmpty()) {
            return "redirect:/librarian/subscriptions";
        }
        
        Subscription subscription = subscriptionOpt.get();
        model.addAttribute("subscription", convertToDTO(subscription));
        model.addAttribute("user", subscription.getUser());
        
        return "librarian/subscription-details";
    }
    
    /**
     * Extend a subscription
     */
    @PostMapping("/{id}/extend")
    public String extendSubscription(
            @PathVariable Long id,
            @RequestParam int additionalDays,
            RedirectAttributes redirectAttributes) {
        
        Optional<Subscription> subscriptionOpt = subscriptionService.getSubscriptionById(id);
        
        if (subscriptionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Abonnement non trouvé");
            return "redirect:/librarian/subscriptions";
        }
        
        Subscription subscription = subscriptionOpt.get();
        subscription.extend(additionalDays);
        subscriptionService.saveSubscription(subscription);
        
        // Notify user using NotificationTriggerService
        User user = subscription.getUser();
        notificationTriggerService.onSubscriptionExtended(user, additionalDays);
        
        redirectAttributes.addFlashAttribute("success", 
                "Abonnement prolongé de " + additionalDays + " jours avec succès");
        
        return "redirect:/librarian/subscriptions/" + id;
    }
    
    /**
     * Add books to a subscription quota
     */
    @PostMapping("/{id}/add-books")
    public String addBooksToQuota(
            @PathVariable Long id,
            @RequestParam int additionalBooks,
            RedirectAttributes redirectAttributes) {
        
        Optional<Subscription> subscriptionOpt = subscriptionService.getSubscriptionById(id);
        
        if (subscriptionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Abonnement non trouvé");
            return "redirect:/librarian/subscriptions";
        }
        
        Subscription subscription = subscriptionOpt.get();
        
        // Premium subscriptions don't have a book quota limit
        if (subscription.getSubscriptionType() == SubscriptionType.PREMIUM) {
            redirectAttributes.addFlashAttribute("warning", 
                    "Les abonnements Premium n'ont pas de limite de quota de livres");
            return "redirect:/librarian/subscriptions/" + id;
        }
        
        subscription.addQuota(additionalBooks);
        subscriptionService.saveSubscription(subscription);
        
        // Notify user using NotificationTriggerService
        User user = subscription.getUser();
        notificationTriggerService.onBooksAddedToQuota(user, additionalBooks);
        
        redirectAttributes.addFlashAttribute("success", 
                additionalBooks + " livre(s) ajoutés au quota avec succès");
        
        return "redirect:/librarian/subscriptions/" + id;
    }
    
    /**
     * Cancel a subscription
     */
    @PostMapping("/{id}/cancel")
    public String cancelSubscription(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        
        Optional<Subscription> subscriptionOpt = subscriptionService.getSubscriptionById(id);
        
        if (subscriptionOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Abonnement non trouvé");
            return "redirect:/librarian/subscriptions";
        }
        
        Subscription subscription = subscriptionOpt.get();
        subscription.setActive(false);
        subscriptionService.saveSubscription(subscription);
        
        // Notify user using NotificationTriggerService
        User user = subscription.getUser();
        notificationTriggerService.onSubscriptionCancelled(user);
        
        redirectAttributes.addFlashAttribute("success", "Abonnement annulé avec succès");
        
        return "redirect:/librarian/subscriptions";
    }
    
    /**
     * Create a new subscription for a user
     */
    @PostMapping("/create")
    public String createSubscription(
            @RequestParam Long userId,
            @RequestParam SubscriptionType subscriptionType,
            RedirectAttributes redirectAttributes) {
        
        try {
            User user = userService.getUserById(userId);
            Subscription subscription = subscriptionService.createSubscription(user, subscriptionType);
            
            // Notify user using NotificationTriggerService
            notificationTriggerService.onSubscriptionPurchased(user, subscriptionType);
            
            redirectAttributes.addFlashAttribute("success",
                    "Abonnement " + subscriptionType.getDisplayName() + " créé avec succès pour " + user.getName());
            
            return "redirect:/librarian/subscriptions/" + subscription.getId();
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Utilisateur non trouvé");
            return "redirect:/librarian/subscriptions";
        }
    }
    
    /**
     * Search for users to create a subscription
     */
    @GetMapping("/user-search")
    public String searchUsers(@RequestParam String query, Model model) {
        // Search for users by name, username, or email containing the query
        List<User> users = userService.getAllStudents().stream()
                .filter(user ->
                    (user.getName() != null && user.getName().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getUsername() != null && user.getUsername().toLowerCase().contains(query.toLowerCase())) ||
                    (user.getEmail() != null && user.getEmail().toLowerCase().contains(query.toLowerCase()))
                )
                .collect(Collectors.toList());
        
        model.addAttribute("users", users);
        model.addAttribute("subscriptionTypes", SubscriptionType.values());
        model.addAttribute("query", query);
        return "librarian/user-search";
    }
    
    /**
     * Convert a Subscription entity to a DTO for display
     */
    private SubscriptionDTO convertToDTO(Subscription subscription) {
        SubscriptionDTO dto = new SubscriptionDTO();
        dto.setId(subscription.getId());
        dto.setUserId(subscription.getUser().getId());
        dto.setUserName(subscription.getUser().getName());
        dto.setUserEmail(subscription.getUser().getEmail());
        dto.setSubscriptionType(subscription.getSubscriptionType());
        dto.setStartDate(subscription.getStartDate());
        dto.setEndDate(subscription.getEndDate());
        dto.setBooksRemaining(subscription.getBooksRemaining());
        dto.setActive(subscription.isActive());
        dto.setValid(subscription.isValid());
        dto.setHasQuotaRemaining(subscription.hasQuotaRemaining());
        
        // Calculate days remaining
        LocalDateTime now = LocalDateTime.now();
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(now, subscription.getEndDate());
        dto.setDaysRemaining(daysRemaining);
        
        return dto;
    }
}
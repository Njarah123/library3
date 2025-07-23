package com.library.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.model.User;
import com.library.service.NotificationTriggerService;
import com.library.service.UserService;

/**
 * Controller for handling account-related operations
 */
@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private NotificationTriggerService notificationTriggerService;
    
    /**
     * Add funds to the user's account
     */
    @PostMapping("/add-funds")
    public String addFunds(@RequestParam("amount") double amount,
                          @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                          RedirectAttributes redirectAttributes,
                          Principal principal) {
        
        if (amount < 0 || amount > 200000) {
            redirectAttributes.addFlashAttribute("error", "Amount must be between 0 and 200,000 Ar.");
            return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/subscriptions/plans";
        }
        
        User user = userService.findByUsername(principal.getName());
        
        // Add funds to the user's balance
        Double currentBalance = user.getBalance();
        if (currentBalance == null) {
            currentBalance = 0.0;
        }
        user.setBalance(currentBalance + amount);
        userService.save(user);
        
        // Create notification using NotificationTriggerService
        notificationTriggerService.onFundsAdded(user, amount);
        
        redirectAttributes.addFlashAttribute("success",
                String.format("%.0f Ar has been successfully added to your account.", amount));
        
        return redirectUrl != null ? "redirect:" + redirectUrl : "redirect:/subscriptions/plans";
    }
}
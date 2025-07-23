package com.library.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MainController {
    
    @GetMapping("/")
    public String home(Authentication authentication) {
        // Si l'utilisateur est déjà connecté, le rediriger vers son dashboard
        if (authentication != null && authentication.isAuthenticated()) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            switch (role) {
                case "ROLE_LIBRARIAN":
                    return "redirect:/librarian/dashboard";
                case "ROLE_STAFF":
                    return "redirect:/staff/dashboard";
                case "ROLE_STUDENT":
                    return "redirect:/student/dashboard";
                default:
                    return "home"; // Page d'accueil
            }
        }
        return "home"; // Page d'accueil pour les non-connectés
    }
    
}

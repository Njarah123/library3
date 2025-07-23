package com.library.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Contrôleur général pour la route /books qui redirige vers la bonne route selon le rôle
 */
@Controller
public class BooksController {

    @GetMapping("/books")
    public String redirectToRoleSpecificBooks(Authentication authentication) {
        // Récupérer le rôle de l'utilisateur connecté
        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String role = authority.getAuthority();
            
            switch (role) {
                case "ROLE_STUDENT":
                    return "redirect:/student/books";
                case "ROLE_STAFF":
                    return "redirect:/staff/books";
                case "ROLE_LIBRARIAN":
                    return "redirect:/librarian/books";
                default:
                    break;
            }
        }
        
        // Par défaut, rediriger vers la page d'accueil si le rôle n'est pas reconnu
        return "redirect:/";
    }
}
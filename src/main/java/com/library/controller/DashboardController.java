package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.library.model.User;
import com.library.security.CustomUserDetails;
import com.library.service.BookService;
import com.library.service.UserService;

@Controller
public class DashboardController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = currentUser.getUser();
        
        // Statistiques globales
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("availableBooks", bookService.getAvailableBooks());
        
        // Pour les administrateurs et bibliothécaires, montrer toutes les statistiques
        if (user.getUserType().toString().equals("STAFF") || 
            user.getUserType().toString().equals("LIBRARIAN")) {
            model.addAttribute("totalBorrowedBooks", bookService.getBorrowedBooksCount());
            model.addAttribute("totalReservedBooks", bookService.getReservedBooksCount());
            model.addAttribute("allBorrowedBooks", bookService.getAllBorrowedBooks());
            model.addAttribute("allReservedBooks", bookService.getAllReservedBooks());
        }
        
        // Livres empruntés et réservés par l'utilisateur courant
        model.addAttribute("borrowedBooks", bookService.getBorrowedBooks(user));
        model.addAttribute("reservedBooks", bookService.getReservedBooks(user));
        
        // Informations de l'utilisateur
        model.addAttribute("user", user);
        
        // Redirection vers le dashboard approprié
        switch (user.getUserType()) {
            case LIBRARIAN:
                return "dashboard/librarian";
            case STAFF:
                return "dashboard/staff";
            case STUDENT:
                return "dashboard/student";
            default:
                return "dashboard/default";
        }
    }
}
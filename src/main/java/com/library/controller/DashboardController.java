package com.library.controller;

import com.library.enums.UserType;
import com.library.model.User;
import com.library.security.CustomUserDetails;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.NotificationService;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private BorrowService borrowService;

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails currentUser, Model model) {
        User user = currentUser.getUser();
        
        // Statistiques globales
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("availableBooks", bookService.getAvailableBooks());
        
        // Pour les administrateurs et bibliothécaires, montrer toutes les statistiques
        if (user.getUserType() == UserType.STAFF || user.getUserType() == UserType.LIBRARIAN) {
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
                model.addAttribute("recentActivities", borrowService.getRecentActivities());
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
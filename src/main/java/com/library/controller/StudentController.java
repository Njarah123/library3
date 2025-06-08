package com.library.controller;

import java.time.LocalDateTime;
import java.util.List; // Changé de Borrow à Borrowing

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // Changé de BorrowService à BorrowingService
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import com.library.service.UserService;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowingService borrowingService; // Changé de borrowService à borrowingService

    @Autowired
    private UserService userService;



@GetMapping("/history")
    public String getBorrowingHistory(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User student = userService.findByUsername(userDetails.getUsername());
            
            // Récupérer l'historique complet des emprunts
            List<Borrowing> borrowingHistory = borrowingService.getBorrowingHistory(student);
            
            // Statistiques
            int totalBorrowings = borrowingHistory.size();
            int totalOverdues = borrowingService.getTotalOverduesCount(student);
            int uniqueBooksCount = borrowingService.getUniqueBorrowedBooksCount(student);
            
            // Ajouter les attributs au modèle
            model.addAttribute("borrowings", borrowingHistory);
            model.addAttribute("totalBorrowings", totalBorrowings);
            model.addAttribute("totalOverdues", totalOverdues);
            model.addAttribute("uniqueBooksCount", uniqueBooksCount);
            model.addAttribute("currentDateTime", LocalDateTime.now());
            
            return "student/history";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Une erreur est survenue lors du chargement de l'historique : " + e.getMessage());
            return "error";
        }
    }




 @GetMapping("/borrows")
    public String getCurrentBorrows(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User student = userService.findByUsername(userDetails.getUsername());
            
            // Récupérer les emprunts actuels
            List<Borrowing> currentBorrows = borrowingService.getCurrentBorrowings(student);
            
            // Ajouter les attributs au modèle
            model.addAttribute("borrows", currentBorrows);
            model.addAttribute("currentDateTime", LocalDateTime.now());
            model.addAttribute("currentBorrowsCount", currentBorrows.size());
            model.addAttribute("maxBorrows", 3);
            
            return "student/borrows";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Une erreur est survenue lors du chargement des emprunts : " + e.getMessage());
            return "error";
        }}

         @PostMapping("/borrows/{borrowId}/return")
    public String returnBook(@PathVariable Long borrowId, 
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            borrowingService.processBookReturn(borrowId);
            redirectAttributes.addFlashAttribute("success", "Livre retourné avec succès!");
            return "redirect:/student/borrows";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du retour : " + e.getMessage());
            return "redirect:/student/borrows";
        }
    }

    
    @PostMapping("/books/{bookId}/borrow")
    public String borrowBook(@PathVariable Long bookId, 
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            User student = userService.findByUsername(userDetails.getUsername());
            borrowingService.createBorrowing(bookId);
            redirectAttributes.addFlashAttribute("success", "Livre emprunté avec succès !");
            return "redirect:/student/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'emprunt : " + e.getMessage());
            return "redirect:/student/books";
        }
    }
    // Page d'accueil étudiant
@GetMapping("/dashboard")
public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
    try {
        User student = userService.findByUsername(userDetails.getUsername());
        
        // Récupérer les statistiques de base
        model.addAttribute("currentBorrows", borrowingService.getCurrentBorrowingsCount(student));
        model.addAttribute("overdueBorrows", borrowingService.getOverdueBorrowingsCount(student));
        model.addAttribute("totalBorrows", borrowingService.getTotalBorrowingsCount(student));
        model.addAttribute("maxBorrows", 3);

        // Récupérer les emprunts à rendre prochainement
        List<Borrowing> upcomingReturns = borrowingService.getUpcomingReturns(student);
        model.addAttribute("upcomingReturns", upcomingReturns);

        // Ajouter l'heure actuelle
        model.addAttribute("currentDateTime", LocalDateTime.now());
        
        return "student/dashboard";
    } catch (Exception e) {
        e.printStackTrace(); // Pour le debugging
        model.addAttribute("error", "Une erreur est survenue lors du chargement du tableau de bord: " + e.getMessage());
        return "error";
    }
}
    

    // Liste des livres disponibles
  @GetMapping("/books")
    public String listBooks(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        try {
            // Récupération de l'utilisateur
            User student = userService.findByUsername(userDetails.getUsername());
            if (student == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }

            // Récupération des livres
            List<Book> books;
            if (search != null && !search.isEmpty() || category != null && !category.isEmpty()) {
                books = bookService.searchBooks(search, category);
            } else {
                books = bookService.getAllBooks();
            }

            // Récupération du nombre d'emprunts en cours
            int currentBorrowings = borrowingService.getCurrentBorrowingsCount(student);

            // Ajout des attributs au modèle
            model.addAttribute("books", books);
            model.addAttribute("categories", bookService.getAllCategories());
            model.addAttribute("currentBorrows", currentBorrowings);
            model.addAttribute("maxBorrows", 3); // Limite fixe d'emprunts
            model.addAttribute("search", search);
            model.addAttribute("selectedCategory", category);

            return "student/books";
        } catch (Exception e) {
            e.printStackTrace(); // Pour le débogage
            model.addAttribute("error", "Une erreur s'est produite lors du chargement des livres: " + e.getMessage());
            return "error";
        }
    }

    // Noter un livre
    @PostMapping("/borrows/{borrowId}/rate")
    public String rateBook(@PathVariable Long borrowId,
                          @RequestParam int rating,
                          @RequestParam(required = false) String comment,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        try {
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("La note doit être comprise entre 1 et 5");
            }

            User student = userService.findByUsername(userDetails.getUsername());
            borrowingService.rateBorrowing(borrowId, student, rating, comment);
            
            redirectAttributes.addFlashAttribute("success", "Merci d'avoir noté ce livre !");
            return "redirect:/student/history";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la notation : " + e.getMessage());
            return "redirect:/student/history";
        }
    }
}


class RatingRequest {
    private int rating;
    private String comment;

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
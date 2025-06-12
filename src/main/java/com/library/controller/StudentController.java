package com.library.controller;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List; // Changé de Borrow à Borrowing

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails; // Changé de BorrowService à BorrowingService
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import com.library.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowingService borrowingService; // Changé de borrowService à borrowingService

    @Autowired
    private UserService userService;
        private static final Logger logger = LoggerFactory.getLogger(StudentController.class);


    

@GetMapping("/books/details/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookDetails(@PathVariable Long id) {
        try {
            Book book = bookService.getBookById(id);
            if (book != null) {
                Map<String, Object> bookDetails = new HashMap<>();
                bookDetails.put("id", book.getId());
                bookDetails.put("title", book.getTitle());
                bookDetails.put("author", book.getAuthor());
                bookDetails.put("description", book.getDescription());
                bookDetails.put("isbn", book.getIsbn());
                bookDetails.put("category", book.getCategory());
                bookDetails.put("publisher", book.getPublisher());
                bookDetails.put("publicationYear", book.getPublicationYear());
                bookDetails.put("language", book.getLanguage());
                bookDetails.put("edition", book.getEdition());
                bookDetails.put("quantity", book.getQuantity());
                bookDetails.put("availableQuantity", book.getAvailableQuantity());
                bookDetails.put("imagePath", book.getImagePath());
                bookDetails.put("rating", book.getRating());
                bookDetails.put("status", book.getAvailable() ? "Disponible" : "Non disponible");

                return ResponseEntity.ok(bookDetails);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }




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
            logger.info("Tentative de récupération des emprunts pour l'utilisateur: {}", userDetails.getUsername());
            
            // Récupérer l'utilisateur
            User student = userService.findByUsername(userDetails.getUsername());
            if (student == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }
            
            // Récupérer les emprunts
            List<Borrowing> currentBorrows = borrowingService.getCurrentBorrowings(student);
            logger.info("Nombre d'emprunts trouvés: {}", currentBorrows.size());
            
            // Ajouter les informations au modèle
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("currentDateTime", 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            model.addAttribute("borrows", currentBorrows);
            model.addAttribute("currentBorrowsCount", currentBorrows.size());
            model.addAttribute("maxBorrows", 3);
            
            return "student/borrows";
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des emprunts: ", e);
            model.addAttribute("error", "Une erreur est survenue: " + e.getMessage());
            return "error";
        }
    }
@PostMapping("/borrows/{borrowId}/return")
    public String returnBook(@PathVariable Long borrowId, 
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            logger.info("Tentative de retour du livre pour l'emprunt: {}", borrowId);
            borrowingService.processBookReturn(borrowId);
            redirectAttributes.addFlashAttribute("success", "Livre retourné avec succès!");
            return "redirect:/student/borrows";
        } catch (Exception e) {
            logger.error("Erreur lors du retour du livre: ", e);
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
        User currentUser = userService.findByUsername(userDetails.getUsername());
        
        model.addAttribute("user", currentUser);
        model.addAttribute("user", student);
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
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
    try {
        User currentUser = userService.findByUsername(userDetails.getUsername());
        
        Page<Book> bookPage;
        if (category != null && !category.isEmpty()) {
            bookPage = bookService.findByCategory(category, PageRequest.of(page, size));
        } else if (search != null && !search.isEmpty()) {
            bookPage = bookService.searchBooks(search, PageRequest.of(page, size));
        } else {
            bookPage = bookService.getAllBooks(PageRequest.of(page, size));
        }

        // Récupérer les catégories
        List<String> categories = bookService.getAllCategories();

        // Format de la date UTC
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("search", search);
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("currentTime", currentDateTime);

        return "student/books";
    } catch (Exception e) {
        model.addAttribute("error", "Une erreur s'est produite: " + e.getMessage());
        return "student/books";
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
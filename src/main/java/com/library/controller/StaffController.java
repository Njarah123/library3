package com.library.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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
import com.library.model.Borrow;
import com.library.model.Borrowing;
import com.library.model.Staff;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.BorrowingService;
import com.library.service.UserService;
import java.util.Map;

@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;
    @Autowired
    private BorrowingService borrowingService;
    @Autowired
    private BorrowService borrowService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Récupérer les emprunts du staff connecté
        List<Borrow> currentBorrows = borrowService.getCurrentBorrowsByStaff(userDetails.getUsername());
        User currentUser = userService.findByUsername(userDetails.getUsername());
        Staff staff = (Staff) userService.findByUsername(userDetails.getUsername());
        model.addAttribute("user", staff);
        model.addAttribute("user", currentUser);
        model.addAttribute("currentBorrows", currentBorrows);
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("currentTime", currentDateTime);
        
        return "staff/dashboard";
    }


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




   @GetMapping("/books")
public String showBooks(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        Model model,
        @AuthenticationPrincipal UserDetails userDetails) {
    try {
        Page<Book> bookPage;
        
        // Logique de recherche et filtrage
        if (search != null && !search.trim().isEmpty()) {
            if (category != null && !category.isEmpty()) {
                // Recherche avec catégorie
                bookPage = bookService.searchByCategoryAndTerm(category, search.trim(), PageRequest.of(page, size));
            } else {
                // Recherche sans catégorie
                bookPage = bookService.searchBooks(search.trim(), PageRequest.of(page, size));
            }
        } else if (category != null && !category.isEmpty()) {
            // Filtrage par catégorie uniquement
            bookPage = bookService.findByCategory(category, PageRequest.of(page, size));
        } else {
            // Aucun filtre
            bookPage = bookService.getAllBooks(PageRequest.of(page, size));
        }

        // Récupérer toutes les catégories
        List<String> categories = bookService.getAllCategories();

        // Formatter la date courante en UTC
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Ajout des attributs au modèle
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("categories", categories);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("search", search); // Ajouter le terme de recherche au modèle
        model.addAttribute("currentDateTime", currentDateTime);
        model.addAttribute("username", userDetails.getUsername());

        return "staff/books";
    } catch (Exception e) {
        model.addAttribute("error", "Une erreur s'est produite: " + e.getMessage());
        return "error";
    }
}

    // Supprimer cette méthode car elle est dupliquée
    /*@PostMapping("/borrow")
    public String borrowBook(@RequestParam Long bookId, Principal principal, RedirectAttributes redirectAttributes) {
        // ... code supprimé car dupliqué
    }*/

    @PostMapping("/rate")
    public String rateBook(
            @RequestParam Long borrowingId, 
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            borrowingService.rateStaffBorrowing(borrowingId, principal.getName(), rating, comment);
            redirectAttributes.addFlashAttribute("success", "Note ajoutée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la notation : " + e.getMessage());
        }
        return "redirect:/staff/history";
    }

    @GetMapping("/history")
    public String showBorrowingHistory(Model model, Principal principal) {
        List<Borrowing> borrowingHistory = borrowingService.getUserBorrowingHistory(principal.getName());
        model.addAttribute("borrowings", borrowingHistory);
        return "staff/history";
    }

    @GetMapping("/borrows")
    public String showBorrows(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        // Récupérer les emprunts du staff connecté
        List<Borrow> currentBorrows = borrowService.getCurrentBorrowsByStaff(userDetails.getUsername());
        
        model.addAttribute("currentBorrows", currentBorrows);
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("currentTime", currentDateTime);
        
        return "staff/borrows";
    }

    @PostMapping("/books/borrow")
    public String borrowBook(
            @RequestParam Long bookId, 
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            // Utiliser la méthode spécifique pour les emprunts du staff
            borrowService.createBorrowForStaff(userDetails.getUsername(), bookId);
            redirectAttributes.addFlashAttribute("success", "Livre emprunté avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'emprunt : " + e.getMessage());
        }
        return "redirect:/staff/books";
    }

    @PostMapping("/borrows/{id}/return")
public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
    try {
        borrowService.returnBook(id);
        redirectAttributes.addFlashAttribute("success", "Livre retourné avec succès !");
    } catch (Exception e) {
        redirectAttributes.addFlashAttribute("error", "Erreur lors du retour : " + e.getMessage());
    }
    return "redirect:/staff/borrows";
}
}
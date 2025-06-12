package com.library.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.service.FileStorageService;
import com.library.service.UserService;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Librarian;
import com.library.model.User;
import com.library.security.CustomUserDetails;
import com.library.service.BookService;
import com.library.service.BorrowingService;

import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.Map;

@Controller
@RequestMapping("/librarian")
@PreAuthorize("hasRole('LIBRARIAN')")
public class LibrarianController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    private final BorrowingService borrowingService;
     @Autowired
    private UserService userService;

    @Autowired
    public LibrarianController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
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
            bookDetails.put("shelfLocation", book.getShelfLocation());
            bookDetails.put("imagePath", book.getImagePath());
            bookDetails.put("rating", book.getRating());
            bookDetails.put("numberOfRatings", book.getNumberOfRatings());
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

    @GetMapping("/borrows/history")
    public String getBorrowingHistory(Model model,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Récupérer tous les emprunts avec pagination
            Page<Borrowing> borrowingsPage = borrowingService.getAllBorrowingsWithPagination(PageRequest.of(page, size));
            
            // Formatter la date courante en UTC
            String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Ajouter les attributs au modèle
            model.addAttribute("borrowings", borrowingsPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", borrowingsPage.getTotalPages());
            model.addAttribute("totalItems", borrowingsPage.getTotalElements());
            model.addAttribute("pageSize", size);
            
            // Ajouter la date/heure et l'utilisateur
            model.addAttribute("currentDateTime", currentDateTime);
            model.addAttribute("username", userDetails.getUsername());
            
            return "librarian/borrowing-history";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement de l'historique : " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/borrowings/{id}/return")
    @ResponseBody
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            borrowingService.returnBook(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors du retour du livre");
        }
    }

    @GetMapping("/borrows-books")
    public String showBorrowedBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        try {
            // Récupération de la page des emprunts
            Page<Borrowing> borrowingPage = borrowingService.getAllBorrowings(page, size);
            
            // Récupération du contenu de la page
            List<Borrowing> allBorrowings = borrowingPage.getContent();
            
            // Formatter la date courante en UTC
            String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Comptage des différents types d'emprunts
            long currentBorrowings = allBorrowings.stream()
                .filter(b -> "EMPRUNTE".equals(b.getStatus()))
                .count();
            
            long overdueBorrowings = allBorrowings.stream()
                .filter(b -> "EMPRUNTE".equals(b.getStatus()) && b.isOverdue())
                .count();
            
            long todayReturns = allBorrowings.stream()
                .filter(b -> "RETOURNE".equals(b.getStatus()) && 
                           b.getReturnDate() != null && 
                           b.getReturnDate().toLocalDate().equals(LocalDate.now()))
                .count();

            // Ajout des attributs de pagination
            model.addAttribute("borrowings", allBorrowings);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", borrowingPage.getTotalPages());
            model.addAttribute("totalItems", borrowingPage.getTotalElements());
            model.addAttribute("pageSize", size);

            // Ajout des statistiques
            model.addAttribute("totalBorrowings", borrowingPage.getTotalElements());
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("overdueBorrowings", overdueBorrowings);
            model.addAttribute("todayReturns", todayReturns);

            // Ajout de la date/heure et de l'utilisateur
            model.addAttribute("currentDateTime", currentDateTime);
            model.addAttribute("username", userDetails.getUsername());

            return "librarian/borrows-books";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Une erreur est survenue: " + e.getMessage());
            return "librarian/borrows-books";
        }
    }
 @GetMapping("/dashboard")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Récupérer l'utilisateur connecté
            User user = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("user", user);

            // Statistiques de base simplifiées
            model.addAttribute("totalBooks", bookService.getTotalBooks());
            model.addAttribute("borrowedBooks", 0); // Temporairement à 0
            model.addAttribute("overdueBooks", 0); // Temporairement à 0

            // Date actuelle
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            model.addAttribute("currentDateTime", LocalDateTime.now().format(formatter));
 Map<String, Long> todayStats = new HashMap<>();
        todayStats.put("emprunts", 0L);  // À remplacer par la vraie valeur plus tard
        todayStats.put("retours", 0L);   // À remplacer par la vraie valeur plus tard
        model.addAttribute("todayStats", todayStats);

        // Ajout des statistiques populaires
        List<Book> popularBooks = bookService.getPopularBooks();
        model.addAttribute("popularBooks", popularBooks);
            return "librarian/dashboard";
        } catch (Exception e) {
            e.printStackTrace();
            return "redirect:/error";
        }
    }

    

   @GetMapping("/books")
public String listBooks(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
    try {
        // Récupération des livres avec pagination et filtrage
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

        return "librarian/books";
    } catch (Exception e) {
        model.addAttribute("error", "Une erreur s'est produite: " + e.getMessage());
        return "error";
    }
}

    @GetMapping("/books/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = fileStorageService.loadFileAsResource(filename);
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG)
            .body(file);
    }

    @GetMapping("/books/add")
    public String showAddBookForm(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Formatter la date courante en UTC
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
        model.addAttribute("currentDateTime", currentDateTime);
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("book", new Book());
        return "librarian/add-book";
    }

    @PostMapping("/books/add")
    public String addBook(@ModelAttribute Book book, 
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                if (!book.getImagePath().startsWith("/images/")) {
                    book.setImagePath("/images/" + book.getImagePath());
                }
            } else {
                book.setImagePath("/images/default-book.png");
            }

            LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
            book.setAddedDate(now);
            book.setLastUpdated(now);

            if (userDetails != null && userDetails.getUser() != null) {
                book.setLibrarian(userDetails.getUser());
            }

            book.setAvailable(true);
            book.setAvailableQuantity(book.getQuantity());
            book.setNumberOfRatings(0);
            book.setRating(0.0);
            book.setTotalBorrows(0);

            bookService.addBook(book);
            redirectAttributes.addFlashAttribute("success", "Livre ajouté avec succès!");
            return "redirect:/librarian/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/librarian/books/add";
        }
    }

    @GetMapping("/books/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, 
                                 Model model, 
                                 @AuthenticationPrincipal UserDetails userDetails) {
        // Formatter la date courante en UTC
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                
        model.addAttribute("currentDateTime", currentDateTime);
        model.addAttribute("username", userDetails.getUsername());
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "librarian/edit-book";
    }

    @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable Long id, 
                           @ModelAttribute("book") Book book, 
                           RedirectAttributes redirectAttributes) {
        try {
            Book existingBook = bookService.getBookById(id);
            if (existingBook == null) {
                throw new RuntimeException("Livre non trouvé");
            }
            
            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            existingBook.setIsbn(book.getIsbn());
            existingBook.setCategory(book.getCategory());
            existingBook.setPublisher(book.getPublisher());
            existingBook.setPublicationYear(book.getPublicationYear());
            existingBook.setLanguage(book.getLanguage());
            existingBook.setEdition(book.getEdition());
            existingBook.setQuantity(book.getQuantity());
            existingBook.setShelfLocation(book.getShelfLocation());
            existingBook.setDescription(book.getDescription());
            existingBook.setPurchasePrice(book.getPurchasePrice());
            existingBook.setReplacementCost(book.getReplacementCost());
            existingBook.setLastUpdated(LocalDateTime.now(ZoneOffset.UTC));
            
            if (book.getImagePath() != null && !book.getImagePath().trim().isEmpty()) {
                if (!book.getImagePath().startsWith("/images/")) {
                    existingBook.setImagePath("/images/" + book.getImagePath());
                } else {
                    existingBook.setImagePath(book.getImagePath());
                }
            }
            
            bookService.updateBook(existingBook);
            redirectAttributes.addFlashAttribute("success", "Livre modifié avec succès!");
            return "redirect:/librarian/books";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            return "redirect:/librarian/books";
        }
    }

    

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookById(id);
            if (book == null) {
                throw new RuntimeException("Livre non trouvé");
            }
            
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Livre supprimé avec succès!");
            return "redirect:/librarian/books";
            
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            return "redirect:/librarian/books";
        }
    }
}
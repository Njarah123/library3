package com.library.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.security.CustomUserDetails;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.BorrowingService;
import com.library.service.FileStorageService;
import com.library.service.SubscriptionService;
import com.library.service.UserService;
import com.library.repository.BorrowingRepository;

@Controller
@RequestMapping("/librarian")
public class LibrarianController {

    @Autowired
    private BookService bookService;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    private final BorrowingService borrowingService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private com.library.service.NotificationService notificationService;
    
    @Autowired
    private com.library.service.NotificationTriggerService notificationTriggerService;

    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private SubscriptionService subscriptionService;

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

// Ajoutez cette méthode dans votre LibrarianController existant

@GetMapping("/reports")
public String showReports(Model model, @AuthenticationPrincipal UserDetails userDetails) {
    try {
        // Récupérer l'utilisateur connecté
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        
        // Statistiques de base (utilisant vos méthodes existantes)
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("totalUsers", userService.getTotalMembers());
        model.addAttribute("activeMembers", userService.getActiveMembersCount());
        model.addAttribute("inactiveMembers", userService.getInactiveMembersCount());
        model.addAttribute("newMembers", userService.getNewMembersCount());
        
        // Statistiques des emprunts (version sécurisée)
        model.addAttribute("totalBorrowings", borrowingService.getTotalBorrowings());
        model.addAttribute("activeBorrowings", borrowingService.getCurrentlyBorrowedCount());
        
        // Utilisez des valeurs par défaut pour éviter les erreurs
        try {
            model.addAttribute("overdueBorrowings", borrowingService.countOverdueBorrowings());
        } catch (Exception e) {
            model.addAttribute("overdueBorrowings", 0L);
        }
        
        try {
            model.addAttribute("todayReturns", borrowingService.countTodayReturns());
        } catch (Exception e) {
            model.addAttribute("todayReturns", 0L);
        }
        
        // Données pour les listes (version sécurisée)
        try {
            model.addAttribute("recentActivities", borrowService.getRecentActivities());
        } catch (Exception e) {
            model.addAttribute("recentActivities", new ArrayList<>());
        }
        
        try {
            model.addAttribute("memberStats", borrowingService.getMemberBorrowingStats());
        } catch (Exception e) {
            model.addAttribute("memberStats", new ArrayList<>());
        }
        
        try {
            model.addAttribute("overdueBooks", borrowingService.getOverdueBooks());
        } catch (Exception e) {
            model.addAttribute("overdueBooks", new ArrayList<>());
        }
        
        // Listes vides pour les données manquantes
        model.addAttribute("topBooks", new ArrayList<>());
        model.addAttribute("topUsers", new ArrayList<>());
        model.addAttribute("monthlyStats", new ArrayList<>());
        
        // Date actuelle
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("currentDateTime", LocalDateTime.now().format(formatter));
        model.addAttribute("username", userDetails.getUsername());
        
        return "librarian/reports";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Erreur lors du chargement des rapports: " + e.getMessage());
        return "error";
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

    @PostMapping("/librarian/borrowings/{id}/return")
    @ResponseBody
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            borrowingService.returnBook(id, "Good");
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
            // Récupérer l'utilisateur connecté
            User user = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
            
            // Récupération de la page des emprunts
            Page<Borrowing> borrowingPage = borrowingService.getAllBorrowings(page, size);
            
            // Récupération du contenu de la page
            List<Borrowing> allBorrowings = borrowingPage.getContent();
            
            // Formatter la date courante en UTC
            String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            
            // Comptage des différents types d'emprunts
            // Comptage des différents types d'emprunts
            long currentBorrowings = borrowingService.getCurrentlyBorrowedCount();
            long overdueBorrowings = borrowingService.countOverdueBorrowings();
            long todayReturns = borrowingService.countTodayReturns();

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

        // Notification count
        long unreadNotifications = notificationService.getUnreadNotificationCount(user);
        model.addAttribute("unreadNotifications", unreadNotifications);
        
        // Récupérer uniquement l'abonnement actif de l'étudiant 'joannahasina'
        List<Subscription> studentSubscriptions = subscriptionService.getAllActiveStudentSubscriptions();
        
        // Ajouter les abonnements au modèle
        model.addAttribute("studentSubscriptions", studentSubscriptions);
        
        // ==================== STATISTIQUES DE BASE ====================
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime endOfToday = startOfToday.plusDays(1).minusSeconds(1);
        
        // Statistiques de base (sûres)
        long todayBorrowsCount = borrowingService.getTodayBorrowingsCount();
        long todayReturnsCount = borrowingService.getTodayReturnsCount();
        long overdueCount = borrowingService.countOverdueBorrowings();
        
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("borrowedBooks", borrowingService.getCurrentlyBorrowedCount());
        model.addAttribute("overdueBooks", overdueCount);
        
        // Statistiques du jour
        Map<String, Long> todayStats = new HashMap<>();
        todayStats.put("emprunts", todayBorrowsCount);
        todayStats.put("retours", todayReturnsCount);
        model.addAttribute("todayStats", todayStats);
        
        // ==================== DONNÉES POUR LES GRAPHIQUES ====================
        
        // 1. Emprunts des 7 derniers jours
        List<Long> weeklyBorrows = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            try {
                LocalDateTime dayStart = LocalDate.now().minusDays(i).atStartOfDay();
                LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);
                long dayBorrows = borrowingService.countBorrowingsByDateRange(dayStart, dayEnd);
                weeklyBorrows.add(dayBorrows);
            } catch (Exception e) {
                weeklyBorrows.add(0L);
            }
        }
        model.addAttribute("weeklyBorrows", weeklyBorrows.toString());
        
        // Get most borrowed books
        List<Book> popularBooks = bookService.getMostBorrowedBooks();
        model.addAttribute("popularBooks", popularBooks);
        
        // 2. Top 5 des catégories de livres - CORRIGÉ
        try {
            Map<String, Long> categoryStats = bookService.getCategoryStatistics();
            
            if (categoryStats.isEmpty()) {
                // Données par défaut si aucune catégorie trouvée
                List<String> defaultCategories = Arrays.asList("Fiction", "Science", "Histoire", "Art", "Technologie");
                List<Long> defaultCounts = Arrays.asList(10L, 8L, 6L, 4L, 3L);
                
                model.addAttribute("bookCategories", defaultCategories.toString().replace("'", "\""));
                model.addAttribute("categoryCounts", defaultCounts.toString());
            } else {
                List<String> topCategories = categoryStats.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                    .limit(5)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
                
                List<Long> categoryCounts = topCategories.stream()
                    .map(categoryStats::get)
                    .collect(Collectors.toList());
                
                // Convertir en JSON proprement
                ObjectMapper mapper = new ObjectMapper();
                String categoriesJson = mapper.writeValueAsString(topCategories);
                String countsJson = mapper.writeValueAsString(categoryCounts);
                
                model.addAttribute("bookCategories", categoriesJson);
                model.addAttribute("categoryCounts", countsJson);
                
                System.out.println("Categories JSON: " + categoriesJson);
                System.out.println("Counts JSON: " + countsJson);
            }
        } catch (Exception e) {
            System.err.println("Erreur catégories: " + e.getMessage());
            // Valeurs par défaut en cas d'erreur
            model.addAttribute("bookCategories", "[\"Fiction\", \"Science\", \"Histoire\", \"Art\", \"Technologie\"]");
            model.addAttribute("categoryCounts", "[10, 8, 6, 4, 3]");
        }
        
        
        // 3. Répartition des types d'utilisateurs - CORRIGÉ
        try {
            long studentCount = userService.countByUserType("STUDENT");
            long staffCount = userService.countByUserType("STAFF");
            long librarianCount = userService.countByUserType("LIBRARIAN");
            
            // Vérifier que nous avons des données
            if (studentCount == 0 && staffCount == 0 && librarianCount == 0) {
                // Données par défaut
                model.addAttribute("userTypeStats", "[15, 8, 3]");
            } else {
                List<Long> userTypeStats = Arrays.asList(studentCount, staffCount, librarianCount);
                model.addAttribute("userTypeStats", userTypeStats.toString());
            }
            
            System.out.println("User stats - Students: " + studentCount + ", Staff: " + staffCount + ", Librarians: " + librarianCount);
        } catch (Exception e) {
            System.err.println("Erreur types utilisateurs: " + e.getMessage());
            model.addAttribute("userTypeStats", "[15, 8, 3]");
        }
        
        
        // ==================== ACTIVITÉ RÉCENTE ====================
        try {
            model.addAttribute("recentActivities", borrowService.getRecentActivities());
        } catch (Exception e) {
            model.addAttribute("recentActivities", new ArrayList<>());
            System.err.println("Erreur lors de la récupération des activités récentes: " + e.getMessage());
        }
        
        // Date actuelle
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        model.addAttribute("currentDateTime", LocalDateTime.now().format(formatter));
        
        
        return "librarian/dashboard";
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Erreur lors du chargement du dashboard: " + e.getMessage());
        return "librarian/dashboard";
    }
}




    

   @GetMapping("/books")
public String listBooks(
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String author,
        @RequestParam(required = false) Integer startYear,
        @RequestParam(required = false) Integer endYear,
        @RequestParam(required = false) Boolean available,
        @RequestParam(required = false) String language,
        @RequestParam(required = false) String sortBy,
        @RequestParam(required = false) Boolean ascending,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "12") int size,
        @AuthenticationPrincipal UserDetails userDetails,
        Model model) {
    try {
        // Utiliser la méthode générique pour filtrer et trier les livres
        Page<Book> bookPage = bookService.getFilteredAndSortedBooks(
            search, category, author, startYear, endYear,
            available, language, sortBy, ascending,
            PageRequest.of(page, size)
        );

        // Récupérer les listes pour les filtres dynamiques
        List<String> categories = bookService.getAllCategories();
        List<String> authors = bookService.getAllAuthors();
        List<Integer> publicationYears = bookService.getAllPublicationYears();
        List<String> languages = bookService.getAllLanguages();

        // Formatter la date courante en UTC
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Ajouter les attributs au modèle
        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookPage.getTotalPages());
        model.addAttribute("search", search);
        
        // Ajouter les listes pour les filtres
        model.addAttribute("categories", categories);
        model.addAttribute("authors", authors);
        model.addAttribute("publicationYears", publicationYears);
        model.addAttribute("languages", languages);
        
        // Ajouter les valeurs sélectionnées pour les filtres
        model.addAttribute("selectedCategory", category);
        model.addAttribute("selectedAuthor", author);
        model.addAttribute("selectedStartYear", startYear);
        model.addAttribute("selectedEndYear", endYear);
        model.addAttribute("selectedAvailable", available);
        model.addAttribute("selectedLanguage", language);
        model.addAttribute("selectedSortBy", sortBy);
        model.addAttribute("selectedAscending", ascending);
        
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
        // Récupérer l'utilisateur connecté
        User user = userService.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);
        
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

            Book savedBook = bookService.addBook(book);

            // Notify students and staff using NotificationTriggerService
            notificationTriggerService.onNewBookAdded(savedBook, userDetails.getUser());

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
                           @AuthenticationPrincipal CustomUserDetails userDetails,
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
            
            Book updatedBook = bookService.updateBook(existingBook);

            // Notify students and staff using NotificationTriggerService
            notificationTriggerService.onBookUpdated(updatedBook, userDetails.getUser());

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
    
    /**
     * Updates the totalBorrows field for all books based on their borrowing history.
     * This endpoint can be used to fix books that have incorrect totalBorrows values.
     *
     * @param redirectAttributes For adding flash messages
     * @return Redirect to the book list page
     */
    @GetMapping("/books/update-total-borrows")
    public String updateAllBooksTotalBorrows(RedirectAttributes redirectAttributes) {
        try {
            // Call the BookService method to update all books' totalBorrows
            int updatedCount = bookService.updateAllBooksTotalBorrows();
            
            redirectAttributes.addFlashAttribute("success",
                "Mise à jour réussie! " + updatedCount + " livres ont été mis à jour avec leur nombre total d'emprunts.");
            
            return "redirect:/librarian/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                "Erreur lors de la mise à jour: " + e.getMessage());
            return "redirect:/librarian/books";
        }
    }

    @GetMapping("/settings")
    public String settings(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername());
            model.addAttribute("user", user);
            
            // Get unread notifications count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);
            
            // Get librarian-specific statistics
            long totalBooks = bookService.getTotalBooks();
            long totalBorrowings = borrowingService.getTotalBorrowings();
            long activeBorrowings = borrowingService.getCurrentlyBorrowedCount();
            long overdueBooks = borrowingService.countOverdueBorrowings();
            
            model.addAttribute("totalBooks", totalBooks);
            model.addAttribute("totalBorrowings", totalBorrowings);
            model.addAttribute("activeBorrowings", activeBorrowings);
            model.addAttribute("overdueBooks", overdueBooks);
            
            return "librarian/settings";
        } catch (Exception e) {
            model.addAttribute("error", "Erreur lors du chargement des paramètres");
            return "error";
        }
    }

    @PostMapping("/settings/profile/update")
    public String updateProfile(@RequestParam String name,
                               @RequestParam String email,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername());
            
            // Validate input
            if (name == null || name.trim().isEmpty()) {
                throw new IllegalArgumentException("Le nom ne peut pas être vide");
            }
            
            if (email == null || email.trim().isEmpty()) {
                throw new IllegalArgumentException("L'email ne peut pas être vide");
            }
            
            // Check if email is already taken by another user
            User existingUser = userService.findByEmail(email.trim());
            if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                throw new IllegalArgumentException("Cette adresse email est déjà utilisée");
            }
            
            // Update user information
            user.setName(name.trim());
            user.setEmail(email.trim());
            
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès!");
            return "redirect:/librarian/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
            return "redirect:/librarian/settings";
        }
    }

    @PostMapping("/settings/password/change")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                @AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername());
            
            // Validate passwords
            if (newPassword == null || newPassword.length() < 6) {
                throw new IllegalArgumentException("Le nouveau mot de passe doit contenir au moins 6 caractères");
            }
            
            if (!newPassword.equals(confirmPassword)) {
                throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
            }
            
            // Change password using UserService
            userService.changePassword(user, currentPassword, newPassword);
            
            redirectAttributes.addFlashAttribute("success", "Mot de passe modifié avec succès!");
            return "redirect:/librarian/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du changement de mot de passe : " + e.getMessage());
            return "redirect:/librarian/settings";
        }
    }

    @PostMapping("/settings/profile-image/upload")
    public String uploadProfileImage(@RequestParam("profileImage") org.springframework.web.multipart.MultipartFile file,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserByUsername(userDetails.getUsername());
            
            if (file.isEmpty()) {
                throw new IllegalArgumentException("Veuillez sélectionner une image");
            }
            
            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Le fichier doit être une image");
            }
            
            // Validate file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("L'image ne doit pas dépasser 5MB");
            }
            
            // Save the profile image using UserService
            String imagePath = userService.saveProfileImage(user, file);
            user.setProfileImagePath(imagePath);
            userService.save(user);
            
            redirectAttributes.addFlashAttribute("success", "Photo de profil mise à jour avec succès!");
            return "redirect:/librarian/settings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du téléchargement : " + e.getMessage());
            return "redirect:/librarian/settings";
        }
    }
}
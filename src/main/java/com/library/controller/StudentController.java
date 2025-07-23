package com.library.controller;

import com.library.dto.BookDetailsDTO;
import com.library.enums.UserType;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Subscription;
import com.library.model.User;
import com.library.repository.BorrowingRepository;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import com.library.service.NotificationService;
import com.library.service.SubscriptionService;
import com.library.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowingService borrowingService;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private NotificationService notificationService;

    private static final Logger logger = LoggerFactory.getLogger(StudentController.class);

    @GetMapping("/books/details/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookDetails(@PathVariable Long id) {
        try {
            logger.info("Fetching book details for ID: {}", id);
            Book book = bookService.getBookById(id);
            if (book != null) {
                // Create a simple DTO object instead of using HashMap
                BookDetailsDTO bookDetails = new BookDetailsDTO();
                bookDetails.setId(book.getId());
                bookDetails.setTitle(book.getTitle());
                bookDetails.setAuthor(book.getAuthor());
                bookDetails.setDescription(book.getDescription());
                bookDetails.setIsbn(book.getIsbn());
                bookDetails.setCategory(book.getCategory());
                bookDetails.setPublisher(book.getPublisher());
                bookDetails.setPublicationYear(book.getPublicationYear());
                bookDetails.setLanguage(book.getLanguage());
                bookDetails.setEdition(book.getEdition());
                bookDetails.setQuantity(book.getQuantity());
                bookDetails.setAvailableQuantity(book.getAvailableQuantity());
                bookDetails.setImagePath(book.getImagePath());
                bookDetails.setRating(book.getRating());
                bookDetails.setStatus(book.getAvailable() ? "Disponible" : "Non disponible");
                bookDetails.setAvailable(book.getAvailable());

                // Log the book details for debugging
                logger.info("Book details for ID {}: {}", id, bookDetails);
                
                // Return the DTO directly, not wrapped in any other structure
                return ResponseEntity.ok().body(bookDetails);
            } else {
                logger.warn("Book not found for ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving book details for ID {}: {}", id, e.getMessage(), e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/borrows")
    public String getCurrentBorrows(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            logger.info("Tentative de récupération des emprunts pour l'utilisateur: {}", userDetails.getUsername());
            
            User student = userService.findByUsername(userDetails.getUsername());
            if (student == null) {
                throw new RuntimeException("Utilisateur non trouvé");
            }
            
            List<Borrowing> currentBorrows = borrowingService.getCurrentBorrowings(student);
            logger.info("Nombre d'emprunts trouvés: {}", currentBorrows.size());
            
            // Create a new list to avoid ConcurrentModificationException
            List<Borrowing> validBorrows = new ArrayList<>();
            
            // Ensure book data is properly loaded
            for (Borrowing borrow : currentBorrows) {
                if (borrow.getBook() != null) {
                    // Access book properties to ensure they're loaded
                    logger.info("Book ID: {}, Title: {}", borrow.getBook().getId(), borrow.getBook().getTitle());
                    
                    // Make sure image paths are properly set
                    if (borrow.getBook().getCoverImageUrl() == null && borrow.getBook().getImagePath() == null) {
                        logger.info("Book has no image paths, setting defaults");
                        borrow.getBook().setImagePath("/images/default-book.png");
                    } else if (borrow.getBook().getCoverImageUrl() == null) {
                        logger.info("Book has no coverImageUrl, using imagePath: {}", borrow.getBook().getImagePath());
                    } else if (borrow.getBook().getImagePath() == null) {
                        logger.info("Book has no imagePath, using coverImageUrl: {}", borrow.getBook().getCoverImageUrl());
                        borrow.getBook().setImagePath(borrow.getBook().getCoverImageUrl());
                    }
                    
                    validBorrows.add(borrow);
                } else {
                    logger.warn("Borrowing with ID {} has null book reference - skipping", borrow.getId());
                }
            }
            
            logger.info("Final valid borrowings count: {}", validBorrows.size());
            
            // Calculate statistics for the new interface
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysFromNow = now.plusDays(3);
            
            int dueSoonCount = 0;
            int overdueCount = 0;
            
            for (Borrowing borrow : validBorrows) {
                if (borrow.getDueDate() != null) {
                    if (borrow.getDueDate().isBefore(now)) {
                        overdueCount++;
                    } else if (borrow.getDueDate().isBefore(threeDaysFromNow)) {
                        dueSoonCount++;
                    }
                }
            }
            
            // Get returned books count for this month
            LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            List<Borrowing> returnedThisMonth = borrowingRepository.findByUserAndReturnDateBetween(
                student, startOfMonth, now);
            
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("user", student); // Add the user object to access profileImagePath
            model.addAttribute("currentDateTime",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            model.addAttribute("borrows", validBorrows);
            model.addAttribute("currentBorrowsCount", validBorrows.size());
            model.addAttribute("maxBorrows", 3);
            
            // Add statistics for the new interface
            model.addAttribute("dueSoonCount", dueSoonCount);
            model.addAttribute("overdueCount", overdueCount);
            model.addAttribute("returnedThisMonth", returnedThisMonth.size());
            
            // Get unread notifications count
            long unreadNotifications = notificationService.getUnreadNotificationCount(student);
            model.addAttribute("unreadNotifications", unreadNotifications);
            
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

    /**
     * This method is now deprecated as we're using the BorrowController's borrow-attempt endpoint
     * Keeping it for backward compatibility
     */
    @PostMapping("/books/{bookId}/borrow")
    public String borrowBook(@PathVariable Long bookId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            // Redirect to the new borrow-attempt endpoint
            return "redirect:/borrow-attempt/" + bookId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'emprunt : " + e.getMessage());
            return "redirect:/student/books";
        }
    }

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
        
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);

            int currentBorrowings = borrowingService.getCurrentBorrowingsCount(user);
            int overdueBooks = borrowingService.getOverdueBooksCount(user);
            
            // Get max borrowings based on subscription type
            int maxBorrowings = 5; // Default fallback
            try {
                Optional<Subscription> subscriptionOpt = subscriptionService.getActiveSubscription(user);
                if (subscriptionOpt.isPresent()) {
                    Subscription subscription = subscriptionOpt.get();
                    maxBorrowings = subscription.getSubscriptionType().getBookQuota();
                    // For display purposes, if it's Integer.MAX_VALUE (Premium), show as "Illimité"
                    if (maxBorrowings == Integer.MAX_VALUE) {
                        model.addAttribute("isUnlimitedBorrowings", true);
                        maxBorrowings = currentBorrowings; // For calculation purposes
                    } else {
                        model.addAttribute("isUnlimitedBorrowings", false);
                    }
                } else {
                    // No active subscription - use default limit
                    logger.info("No active subscription found for user: {}, using default limit", user.getUsername());
                    model.addAttribute("isUnlimitedBorrowings", false);
                }
            } catch (Exception e) {
                logger.error("Error retrieving subscription for borrowing limits: {}", user.getUsername(), e);
                model.addAttribute("isUnlimitedBorrowings", false);
            }
        
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("maxBorrowings", maxBorrowings);
            model.addAttribute("overdueBooks", overdueBooks);
        
            int booksReadThisMonth = borrowingService.getBooksReadThisMonth(user);
            int monthlyGoal = 5;
            int readingPoints = calculateReadingPoints(user);
            int readingLevel = calculateReadingLevel(readingPoints);
        
            model.addAttribute("booksReadThisMonth", booksReadThisMonth);
            model.addAttribute("monthlyGoal", monthlyGoal);
            model.addAttribute("readingPoints", readingPoints);
            model.addAttribute("readingLevel", readingLevel);
        
            List<Integer> weeklyReadingData = borrowingService.getWeeklyReadingData(user);
            List<Integer> monthlyReadingData = borrowingService.getMonthlyReadingData(user);
        
            model.addAttribute("weeklyReadingData", weeklyReadingData);
            model.addAttribute("monthlyReadingData", monthlyReadingData);
        
            Map<String, Long> categoryStats = borrowingService.getCategoryPreferences(user);
            List<String> categoryPreferences = new ArrayList<>(categoryStats.keySet());
            List<Long> categoryReadCounts = new ArrayList<>(categoryStats.values());
        
            model.addAttribute("categoryPreferences", categoryPreferences);
            model.addAttribute("categoryReadCounts", categoryReadCounts);
        
            int remainingBooks = Math.max(0, monthlyGoal - booksReadThisMonth);
            int goalPercentage = monthlyGoal > 0 ? (booksReadThisMonth * 100) / monthlyGoal : 0;
            int goalProgress = Math.min(360, (goalPercentage * 360) / 100);
        
            model.addAttribute("remainingBooks", remainingBooks);
            model.addAttribute("goalPercentage", goalPercentage);
            model.addAttribute("goalProgress", goalProgress);
        
            List<Borrowing> currentBorrowingsList = borrowingService.getCurrentBorrowingsByUser(user);
            model.addAttribute("currentBorrowingsList", currentBorrowingsList);
        
            List<Book> recommendedBooks = bookService.getRecommendedBooksForUser(user);
            model.addAttribute("recommendedBooks", recommendedBooks);
        
            int weeklyReading = borrowingService.getWeeklyReadingCount(user);
            int monthlyReading = booksReadThisMonth;
            int totalBooksRead = borrowingService.getTotalBooksReadByUser(user);
        
            model.addAttribute("weeklyReading", weeklyReading);
            model.addAttribute("monthlyReading", monthlyReading);
            model.addAttribute("totalBooksRead", totalBooksRead);
            
            // Add subscription data to the model
            try {
                Optional<Subscription> subscriptionOpt = subscriptionService.getActiveSubscription(user);
                if (subscriptionOpt.isPresent()) {
                    // Convert to DTO for display
                    model.addAttribute("subscription",
                        com.library.dto.SubscriptionDTO.fromSubscription(subscriptionOpt.get()));
                } else {
                    logger.info("No active subscription found for user: {}", user.getUsername());
                }
            } catch (Exception e) {
                logger.error("Error retrieving subscription for user: {}", user.getUsername(), e);
                // Don't let subscription errors prevent dashboard from loading
            }
        
            return "student/dashboard";
        
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du dashboard étudiant", e);
            model.addAttribute("error", "Erreur lors du chargement du dashboard");
            return "error";
        }
    }

    private int calculateReadingPoints(User user) {
        int totalBooks = borrowingService.getTotalBooksReadByUser(user);
        int onTimeReturns = borrowingService.getOnTimeReturnsCount(user);
        int reviews = borrowingService.getReviewsCount(user);
        
        return (totalBooks * 10) + (onTimeReturns * 5) + (reviews * 15);
    }

    private int calculateReadingLevel(int points) {
        if (points < 50) return 1;
        if (points < 150) return 2;
        if (points < 300) return 3;
        if (points < 500) return 4;
        if (points < 750) return 5;
        return 6;
    }

    private List<Integer> getMonthlyBorrowsData(User user) {
        List<Integer> monthlyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime startOfMonth = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
            
            List<Borrowing> borrowings = borrowingRepository.findByUserAndBorrowDateBetween(
                user, startOfMonth, endOfMonth);
            monthlyData.add(borrowings.size());
        }
        
        return monthlyData;
    }
    
    private List<String> getStudentCategories(User user) {
        return borrowingRepository.findDistinctCategoriesByUser(user);
    }
    
    private List<Integer> getStudentCategoryCounts(User user) {
        List<String> categories = getStudentCategories(user);
        List<Integer> counts = new ArrayList<>();
        
        for (String category : categories) {
            int count = borrowingRepository.countByUserAndBookCategory(user, category);
            counts.add(count);
        }
        
        return counts;
    }
    
    private List<Integer> getBorrowStatusData(User user) {
        List<Integer> statusData = new ArrayList<>();
        
        statusData.add(borrowingService.getCurrentBorrowingsCount(user));
        
        statusData.add(borrowingService.getOverdueBorrowsCount(user));
        
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        List<Borrowing> completedThisMonth = borrowingRepository.findByUserAndReturnDateBetween(
            user, startOfMonth, LocalDateTime.now());
        statusData.add(completedThisMonth.size());
        
        return statusData;
    }
    
    private List<Integer> getWeeklyActivityData(User user) {
        List<Integer> weeklyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1).minusSeconds(1);
            
            List<Borrowing> dayBorrows = borrowingRepository.findByUserAndBorrowDateBetween(
                user, dayStart, dayEnd);
            weeklyData.add(dayBorrows.size());
        }
        
        return weeklyData;
    }
    
    private List<Double> getRatingsData(User user) {
        List<Double> ratings = new ArrayList<>();
        List<String> categories = getStudentCategories(user);
        
        for (String category : categories) {
            Double avgRating = borrowingRepository.getAverageRatingByUserAndCategory(user, category);
            ratings.add(avgRating != null ? avgRating : 0.0);
        }
        
        while (ratings.size() < 5) {
            ratings.add(0.0);
        }
        
        return ratings.subList(0, Math.min(5, ratings.size()));
    }
    
    private List<Integer> getProgressData(User user) {
        List<Integer> progressData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 11; i >= 0; i--) {
            LocalDateTime startOfMonth = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusSeconds(1);
            
            List<Borrowing> monthlyBorrows = borrowingRepository.findByUserAndReturnDateBetween(
                user, startOfMonth, endOfMonth);
            progressData.add(monthlyBorrows.size());
        }
        
        return progressData;
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
            User currentUser = userService.findByUsername(userDetails.getUsername());
            
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
            
            model.addAttribute("username", userDetails.getUsername());
            model.addAttribute("currentTime", currentDateTime);

            return "student/books";
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des livres: ", e);
            model.addAttribute("error", "Une erreur s'est produite: " + e.getMessage());
            return "student/books";
        }
    }

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
            return "redirect:/student/borrows";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la notation : " + e.getMessage());
            return "redirect:/student/borrows";
        }
    }

    @GetMapping("/settings")
    public String settings(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            
            // Get unread notifications count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);
            
            // Get subscription information
            try {
                Optional<Subscription> subscriptionOpt = subscriptionService.getActiveSubscription(user);
                if (subscriptionOpt.isPresent()) {
                    model.addAttribute("subscription",
                        com.library.dto.SubscriptionDTO.fromSubscription(subscriptionOpt.get()));
                }
            } catch (Exception e) {
                logger.error("Error retrieving subscription for settings page: {}", user.getUsername(), e);
            }
            
            // Get account information
            if (user.getAccount() != null) {
                model.addAttribute("account", user.getAccount());
            }
            
            // Get borrowing statistics
            int totalBorrowings = borrowingService.getTotalBooksReadByUser(user);
            int currentBorrowings = borrowingService.getCurrentBorrowingsCount(user);
            int overdueBooks = borrowingService.getOverdueBooksCount(user);
            
            model.addAttribute("totalBorrowings", totalBorrowings);
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("overdueBooks", overdueBooks);
            
            return "student/settings";
        } catch (Exception e) {
            logger.error("Error loading student settings page", e);
            model.addAttribute("error", "Erreur lors du chargement des paramètres");
            return "error";
        }
    }

    @PostMapping("/settings/profile/update")
    public String updateProfile(@RequestParam String name,
                               @RequestParam String email,
                               Principal principal,
                               RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName());
            
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
            return "redirect:/student/settings";
        } catch (Exception e) {
            logger.error("Error updating student profile", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
            return "redirect:/student/settings";
        }
    }

    @PostMapping("/settings/password/change")
    public String changePassword(@RequestParam String currentPassword,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                Principal principal,
                                RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName());
            
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
            return "redirect:/student/settings";
        } catch (Exception e) {
            logger.error("Error changing student password", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du changement de mot de passe : " + e.getMessage());
            return "redirect:/student/settings";
        }
    }

    @PostMapping("/settings/profile-image/upload")
    public String uploadProfileImage(@RequestParam("profileImage") org.springframework.web.multipart.MultipartFile file,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName());
            
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
            return "redirect:/student/settings";
        } catch (Exception e) {
            logger.error("Error uploading profile image", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du téléchargement : " + e.getMessage());
            return "redirect:/student/settings";
        }
    }

    @GetMapping("/subscription/details/{id}")
    public String subscriptionDetails(@PathVariable Long id,
                                    Principal principal,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            
            // Get unread notifications count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);
            
            // Get subscription by ID and verify it belongs to the current user
            Optional<Subscription> subscriptionOpt = subscriptionService.getSubscriptionById(id);
            
            if (!subscriptionOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Abonnement non trouvé");
                return "redirect:/student/dashboard";
            }
            
            Subscription subscription = subscriptionOpt.get();
            
            // Verify that this subscription belongs to the current user
            if (!subscription.getUser().getId().equals(user.getId())) {
                redirectAttributes.addFlashAttribute("error", "Accès non autorisé à cet abonnement");
                return "redirect:/student/dashboard";
            }
            
            // Convert to DTO for display
            model.addAttribute("subscription",
                com.library.dto.SubscriptionDTO.fromSubscription(subscription));
            
            return "student/subscription-details";
            
        } catch (Exception e) {
            logger.error("Error loading subscription details for ID: {}", id, e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du chargement des détails de l'abonnement");
            return "redirect:/student/dashboard";
        }
    }

    @GetMapping("/goals")
    public String goals(Principal principal, Model model) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            
            // Get unread notifications count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);
            
            // Get reading statistics for goals
            int booksReadThisMonth = borrowingService.getBooksReadThisMonth(user);
            int booksReadThisYear = borrowingService.getTotalBooksReadByUser(user);
            int currentBorrowings = borrowingService.getCurrentBorrowingsCount(user);
            
            // Default goals (can be made configurable later)
            int monthlyGoal = 5;
            int yearlyGoal = 50;
            
            // Calculate progress
            int monthlyProgress = monthlyGoal > 0 ? (booksReadThisMonth * 100) / monthlyGoal : 0;
            int yearlyProgress = yearlyGoal > 0 ? (booksReadThisYear * 100) / yearlyGoal : 0;
            
            // Get weekly reading data for chart
            List<Integer> weeklyReadingData = borrowingService.getWeeklyReadingData(user);
            List<Integer> monthlyReadingData = borrowingService.getMonthlyReadingData(user);
            
            // Get category preferences
            Map<String, Long> categoryStats = borrowingService.getCategoryPreferences(user);
            
            model.addAttribute("booksReadThisMonth", booksReadThisMonth);
            model.addAttribute("booksReadThisYear", booksReadThisYear);
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("monthlyGoal", monthlyGoal);
            model.addAttribute("yearlyGoal", yearlyGoal);
            model.addAttribute("monthlyProgress", monthlyProgress);
            model.addAttribute("yearlyProgress", yearlyProgress);
            model.addAttribute("weeklyReadingData", weeklyReadingData);
            model.addAttribute("monthlyReadingData", monthlyReadingData);
            model.addAttribute("categoryStats", categoryStats);
            
            return "student/goals";
            
        } catch (Exception e) {
            logger.error("Error loading goals page for user: {}", principal.getName(), e);
            model.addAttribute("error", "Erreur lors du chargement de la page des objectifs");
            return "error";
        }
    }

    @GetMapping("/my-books")
    public String myBooks(Principal principal, Model model) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            
            // Get unread notifications count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);
            
            // Get user's borrowing history to show "my books"
            List<Borrowing> borrowingHistory = borrowingService.getBorrowingHistory(user);
            List<Borrowing> currentBorrowings = borrowingService.getCurrentBorrowings(user);
            
            // Get unique books the user has borrowed (for "My Library" concept)
            List<Book> borrowedBooks = new ArrayList<>();
            Set<Long> bookIds = new HashSet<>();
            
            for (Borrowing borrowing : borrowingHistory) {
                if (borrowing.getBook() != null && !bookIds.contains(borrowing.getBook().getId())) {
                    borrowedBooks.add(borrowing.getBook());
                    bookIds.add(borrowing.getBook().getId());
                }
            }
            
            // Get reading statistics
            int totalBooksRead = borrowingService.getTotalBooksReadByUser(user);
            int currentlyBorrowed = currentBorrowings.size();
            int uniqueBooksRead = borrowedBooks.size();
            
            // Get favorite categories based on borrowing history
            Map<String, Long> categoryStats = borrowingService.getCategoryPreferences(user);
            
            model.addAttribute("borrowedBooks", borrowedBooks);
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("totalBooksRead", totalBooksRead);
            model.addAttribute("currentlyBorrowed", currentlyBorrowed);
            model.addAttribute("uniqueBooksRead", uniqueBooksRead);
            model.addAttribute("categoryStats", categoryStats);
            
            return "student/my-books";
            
        } catch (Exception e) {
            logger.error("Error loading my-books page for user: {}", principal.getName(), e);
            model.addAttribute("error", "Erreur lors du chargement de ma bibliothèque");
            return "error";
        }
    }
}
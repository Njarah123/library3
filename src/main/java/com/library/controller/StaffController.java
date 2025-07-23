package com.library.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.dto.Activity;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.User;
import com.library.service.BookService;
import com.library.service.BorrowingService;
import com.library.service.UserService;
@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private BookService bookService;
    @Autowired
    private UserService userService;

    @Autowired
    private com.library.service.NotificationService notificationService;
    @Autowired
    private BorrowingService borrowingService;
    // Créer le logger avec LoggerFactory (pas d'injection)
    private static final Logger logger = LoggerFactory.getLogger(StaffController.class);

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            
            // Notification count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);

            // Statistiques de base
            int currentBorrowings = borrowingService.getCurrentBorrowingsCount(user);
            int maxBorrowings = 10; // Limite pour le personnel
            int overdueBorrowings = borrowingService.getOverdueBorrowingsCount(user);
            int totalBorrowings = borrowingService.getTotalBorrowingsCount(user);
            Double averageRating = borrowingService.getAverageRatingByUser(user);
            
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("maxBorrowings", maxBorrowings);
            model.addAttribute("overdueBorrowings", overdueBorrowings);
            model.addAttribute("totalBorrowings", totalBorrowings);
            model.addAttribute("averageRating", averageRating);
            
            // Statistiques de lecture
            int booksReadThisMonth = borrowingService.getBooksReadThisMonth(user);
            int monthlyGoal = 5; // Objectif mensuel par défaut
            int readingPoints = calculateReadingPoints(user);
            int readingLevel = calculateReadingLevel(readingPoints);
            
            model.addAttribute("booksReadThisMonth", booksReadThisMonth);
            model.addAttribute("monthlyGoal", monthlyGoal);
            model.addAttribute("readingPoints", readingPoints);
            model.addAttribute("readingLevel", readingLevel);
            
            // Données pour les graphiques
            List<Integer> weeklyReadingData = borrowingService.getWeeklyReadingData(user);
            List<Integer> monthlyReadingData = borrowingService.getMonthlyReadingData(user);
            
            model.addAttribute("weeklyReadingData", weeklyReadingData);
            model.addAttribute("monthlyReadingData", monthlyReadingData);
            
            // Préférences de catégories
            Map<String, Long> categoryStats = borrowingService.getCategoryPreferences(user);
            List<String> categoryPreferences = new ArrayList<>(categoryStats.keySet());
            List<Long> categoryReadCounts = new ArrayList<>(categoryStats.values());
            
            model.addAttribute("categoryPreferences", categoryPreferences);
            model.addAttribute("categoryReadCounts", categoryReadCounts);
            
            // Calcul de la progression de l'objectif
            int remainingBooks = Math.max(0, monthlyGoal - booksReadThisMonth);
            int goalPercentage = monthlyGoal > 0 ? (booksReadThisMonth * 100) / monthlyGoal : 0;
            int goalProgress = Math.min(360, (goalPercentage * 360) / 100);
            
            model.addAttribute("remainingBooks", remainingBooks);
            model.addAttribute("goalPercentage", goalPercentage);
            model.addAttribute("goalProgress", goalProgress);
            
            // Liste des emprunts actuels
            List<Borrowing> currentBorrowingsList = borrowingService.getCurrentBorrowingsByUser(user);
            model.addAttribute("currentBorrowingsList", currentBorrowingsList);
            
            // Statistiques supplémentaires
            int weeklyReading = borrowingService.getWeeklyReadingCount(user);
            int monthlyReading = booksReadThisMonth;
            int totalBooksRead = borrowingService.getTotalBooksReadByUser(user);
            
            model.addAttribute("weeklyReading", weeklyReading);
            model.addAttribute("monthlyReading", monthlyReading);
            model.addAttribute("totalBooksRead", totalBooksRead);
            
            // Activités récentes
            List<Activity> recentActivities = getRecentActivities(user);
            model.addAttribute("recentActivities", recentActivities);
            
            return "staff/dashboard";
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement du dashboard personnel", e);
            model.addAttribute("error", "Erreur lors du chargement du dashboard");
            return "error";
        }
    }

    private List<Activity> getRecentActivities(User user) {
        List<Activity> activities = new ArrayList<>();
        
        try {
            // Récupérer les 5 derniers emprunts
            List<Borrowing> recentBorrowings = borrowingService.getRecentBorrowingsByUser(user, 5);
            
            for (Borrowing borrowing : recentBorrowings) {
                Activity activity = new Activity();
                activity.setTitle("Emprunt de livre");
                activity.setDescription("\"" + borrowing.getBook().getTitle() + "\" par " + borrowing.getBook().getAuthor());
                activity.setTimestamp(borrowing.getBorrowDate());
                activities.add(activity);
            }
            
            // Récupérer les 3 derniers retours
            List<Borrowing> recentReturns = borrowingService.getRecentReturnsByUser(user, 3);
            
            for (Borrowing borrowing : recentReturns) {
                if (borrowing.getReturnDate() != null) {
                    Activity activity = new Activity();
                    activity.setTitle("Retour de livre");
                    activity.setDescription("\"" + borrowing.getBook().getTitle() + "\" rendu");
                    activity.setTimestamp(borrowing.getReturnDate());
                    activities.add(activity);
                }
            }
            
            // Trier par date décroissante et limiter à 5
            return activities.stream()
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                    .limit(5)
                    .collect(Collectors.toList());
            
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des activités récentes", e);
                 return new ArrayList<>();
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
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer startYear,
            @RequestParam(required = false) Integer endYear,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Boolean ascending,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Page<Book> bookPage;
            
            // Logique de recherche et filtrage avancé
            if (sortBy != null || author != null || startYear != null || endYear != null || available != null || language != null) {
                // Utiliser la méthode de filtrage et tri avancé
                bookPage = bookService.getFilteredAndSortedBooks(
                    search, category, author, startYear, endYear, available, language, sortBy, ascending, PageRequest.of(page, size));
            } else if (search != null && !search.trim().isEmpty()) {
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

            // Récupérer toutes les catégories, auteurs et langues pour les filtres
            List<String> categories = bookService.getAllCategories();
            List<String> authors = bookService.getAllAuthors();
            List<String> languages = bookService.getAllLanguages();

            // Formatter la date courante en UTC
            String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // Ajout des attributs au modèle
            model.addAttribute("books", bookPage.getContent());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", bookPage.getTotalPages());
            model.addAttribute("categories", categories);
            model.addAttribute("authors", authors);
            model.addAttribute("languages", languages);
            model.addAttribute("selectedCategory", category);
            model.addAttribute("selectedAuthor", author);
            model.addAttribute("selectedStartYear", startYear);
            model.addAttribute("selectedEndYear", endYear);
            model.addAttribute("selectedAvailable", available);
            model.addAttribute("selectedLanguage", language);
            model.addAttribute("selectedSortBy", sortBy);
            model.addAttribute("selectedAscending", ascending);
            model.addAttribute("search", search);
            model.addAttribute("currentDateTime", currentDateTime);
            model.addAttribute("username", userDetails.getUsername());

            return "staff/books";
        } catch (Exception e) {
            model.addAttribute("error", "Une erreur s'est produite: " + e.getMessage());
            return "error";
        }
    }

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
        
        User user = userService.findByUsername(userDetails.getUsername());
        List<Borrowing> currentBorrows = borrowingService.getCurrentBorrowingsByUser(user);
        
        model.addAttribute("currentBorrows", currentBorrows);
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("user", user); // Add the user object to access profileImagePath
        model.addAttribute("currentTime", currentDateTime);
        
        return "staff/borrows";
    }

    @GetMapping("/books/borrow-confirmation/{bookId}")
    public String showBorrowConfirmation(@PathVariable Long bookId, Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            Book book = bookService.getBookById(bookId);
            
            if (book == null) {
                model.addAttribute("error", "Livre non trouvé");
                return "error";
            }
            
            if (book.getAvailableQuantity() <= 0) {
                model.addAttribute("error", "Ce livre n'est plus disponible");
                return "error";
            }
            
            // Vérifier les limites d'emprunt pour le personnel (10 livres max)
            int currentBorrowings = borrowingService.getCurrentBorrowingsCount(user);
            int maxBorrowings = 10; // Limite pour le personnel
            
            if (currentBorrowings >= maxBorrowings) {
                model.addAttribute("error", "Vous avez atteint la limite d'emprunts simultanés (" + maxBorrowings + " livres)");
                return "error";
            }
            
            // Calculer la date de retour (30 jours pour le personnel)
            LocalDateTime expectedReturnDate = LocalDateTime.now().plusDays(30);
            
            // Ajouter les données au modèle
            model.addAttribute("book", book);
            model.addAttribute("user", user);
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("maxBorrowings", maxBorrowings);
            model.addAttribute("expectedReturnDate", expectedReturnDate);
            
            return "staff/borrow-confirmation";
        } catch (Exception e) {
            logger.error("Erreur lors de l'affichage de la confirmation d'emprunt", e);
            model.addAttribute("error", "Erreur lors du chargement de la page de confirmation");
            return "error";
        }
    }

    @PostMapping("/books/borrow")
    public String borrowBook(
            @RequestParam Long bookId,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            borrowingService.borrowBook(bookId, userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Livre emprunté avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'emprunt : " + e.getMessage());
        }
        return "redirect:/staff/books";
    }

    @PostMapping("/borrows/{id}/return")
    public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            borrowingService.returnBook(id, "Good");
            redirectAttributes.addFlashAttribute("success", "Livre retourné avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors du retour : " + e.getMessage());
        }
        return "redirect:/staff/borrows";
    }

    @GetMapping("/settings")
    public String settings(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            
            // Get unread notifications count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);
            
            // Get borrowing statistics
            int totalBorrowings = borrowingService.getTotalBooksReadByUser(user);
            int currentBorrowings = borrowingService.getCurrentBorrowingsCount(user);
            int overdueBooks = borrowingService.getOverdueBooksCount(user);
            int readingLevel = calculateReadingLevel(calculateReadingPoints(user));
            
            model.addAttribute("totalBorrowings", totalBorrowings);
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("overdueBorrowings", overdueBooks);
            model.addAttribute("readingLevel", readingLevel);
            
            return "staff/settings";
        } catch (Exception e) {
            logger.error("Error loading staff settings page", e);
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
            return "redirect:/staff/settings";
        } catch (Exception e) {
            logger.error("Error updating staff profile", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
            return "redirect:/staff/settings";
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
            return "redirect:/staff/settings";
        } catch (Exception e) {
            logger.error("Error changing staff password", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du changement de mot de passe : " + e.getMessage());
            return "redirect:/staff/settings";
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
            return "redirect:/staff/settings";
        } catch (Exception e) {
            logger.error("Error uploading staff profile image", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors du téléchargement : " + e.getMessage());
            return "redirect:/staff/settings";
        }
    }

    @GetMapping("/goals")
    public String goals(Model model, Principal principal) {
        try {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("user", user);
            
            // Notification count
            long unreadNotifications = notificationService.getUnreadNotificationCount(user);
            model.addAttribute("unreadNotifications", unreadNotifications);

            // Statistiques de lecture pour les objectifs
            int booksReadThisMonth = borrowingService.getBooksReadThisMonth(user);
            int totalBooksRead = borrowingService.getTotalBooksReadByUser(user);
            int monthlyGoal = 8; // Objectif mensuel pour le personnel (plus élevé que les étudiants)
            int yearlyGoal = 96; // Objectif annuel pour le personnel
            
            // Estimation des livres lus cette année (approximation basée sur le total)
            int booksReadThisYear = Math.min(totalBooksRead, yearlyGoal);
            
            // Calcul des pourcentages de progression
            int monthlyProgress = monthlyGoal > 0 ? (booksReadThisMonth * 100) / monthlyGoal : 0;
            int yearlyProgress = yearlyGoal > 0 ? (booksReadThisYear * 100) / yearlyGoal : 0;
            
            model.addAttribute("booksReadThisMonth", booksReadThisMonth);
            model.addAttribute("booksReadThisYear", booksReadThisYear);
            model.addAttribute("monthlyGoal", monthlyGoal);
            model.addAttribute("yearlyGoal", yearlyGoal);
            model.addAttribute("monthlyProgress", monthlyProgress);
            model.addAttribute("yearlyProgress", yearlyProgress);
            
            // Données pour les graphiques
            List<Integer> weeklyReadingData = borrowingService.getWeeklyReadingData(user);
            List<Integer> monthlyReadingData = borrowingService.getMonthlyReadingData(user);
            
            model.addAttribute("weeklyReadingData", weeklyReadingData);
            model.addAttribute("monthlyReadingData", monthlyReadingData);
            
            // Préférences de catégories
            Map<String, Long> categoryStats = borrowingService.getCategoryPreferences(user);
            model.addAttribute("categoryStats", categoryStats);
            
            return "staff/goals";
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la page des objectifs du personnel", e);
            model.addAttribute("error", "Erreur lors du chargement de la page des objectifs");
            return "error";
        }
    }
}
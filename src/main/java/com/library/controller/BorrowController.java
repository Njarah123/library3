package com.library.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.enums.BorrowingStatus;
import com.library.enums.NotificationType;
import com.library.enums.UserType;
import com.library.model.Book;
import com.library.model.Borrow;
import com.library.model.User;
import com.library.security.CustomUserDetails;
import com.library.service.BookService;
import com.library.service.BorrowService;
import com.library.service.NotificationService;
import com.library.service.NotificationTriggerService;
import com.library.service.SubscriptionService;

@Controller
public class BorrowController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private SubscriptionService subscriptionService;
    
    @Autowired
    private com.library.service.BorrowingService borrowingService;
    
    @Autowired
    private NotificationTriggerService notificationTriggerService;

    /**
     * This endpoint handles the initial borrow attempt and checks subscription status
     */
    @GetMapping("/borrow-attempt/{bookId}")
    public String attemptBorrowBook(@PathVariable Long bookId,
                                  @AuthenticationPrincipal CustomUserDetails userDetails,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        // Vérifier si l'utilisateur est connecté
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté pour emprunter un livre");
            return "redirect:/login";
        }

        try {
            User user = userDetails.getUser();
            Book book = bookService.getBookById(bookId);
            
            if (book == null) {
                redirectAttributes.addFlashAttribute("error", "Livre non trouvé");
                return redirectToUserBooks(user);
            }
            
            // Vérifier si le livre est disponible
            if (book.getAvailableQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Ce livre n'est pas disponible pour l'emprunt");
                return redirectToUserBooks(user);
            }
            
            // Vérifier si l'utilisateur a déjà emprunté ce livre
            if (borrowService.hasUserBorrowedBook(user.getId(), bookId)) {
                redirectAttributes.addFlashAttribute("error", "Vous avez déjà emprunté ce livre");
                return redirectToUserBooks(user);
            }
            
            // If user is not a student, they can borrow without subscription
            if (user.getUserType() != UserType.STUDENT) {
                return "redirect:/borrow/" + bookId;
            }
            
            // For students, check if they have an active subscription
            boolean hasActiveSubscription = subscriptionService.hasActiveSubscription(user);
            
            // If they have an active subscription and can borrow more books, proceed with borrowing
            if (hasActiveSubscription && subscriptionService.canBorrowBook(user)) {
                return "redirect:/borrow/" + bookId;
            }
            
            // Otherwise, redirect to the subscription plans page
            // Add bookId as a parameter to return to borrowing after subscription
            redirectAttributes.addFlashAttribute("info", "Vous avez besoin d'un abonnement actif pour emprunter ce livre");
            return "redirect:/subscriptions/plans?bookId=" + bookId;
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'emprunt: " + e.getMessage());
            return "redirect:/error/borrow";
        }
    }

    /**
     * This endpoint displays a confirmation page before borrowing a book
     */
    @GetMapping("/borrow/{bookId}")
    public String showBorrowConfirmation(@PathVariable Long bookId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {
        // Vérifier si l'utilisateur est connecté
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté pour emprunter un livre");
            return "redirect:/login";
        }

        try {
            User user = userDetails.getUser();
            Book book = bookService.getBookById(bookId);
            
            if (book == null) {
                redirectAttributes.addFlashAttribute("error", "Livre non trouvé");
                return redirectToUserBooks(user);
            }
            
            // Vérifier si le livre est disponible
            if (book.getAvailableQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Ce livre n'est pas disponible pour l'emprunt");
                return redirectToUserBooks(user);
            }
            
            // Vérifier si l'utilisateur a déjà emprunté ce livre
            if (borrowService.hasUserBorrowedBook(user.getId(), bookId)) {
                redirectAttributes.addFlashAttribute("error", "Vous avez déjà emprunté ce livre");
                return redirectToUserBooks(user);
            }
            
            // Check if user is a student and needs a subscription
            if (user.getUserType() == UserType.STUDENT) {
                // Check if student has an active subscription with remaining quota
                if (!subscriptionService.hasActiveSubscription(user)) {
                    redirectAttributes.addFlashAttribute("error",
                        "Vous devez avoir un abonnement actif pour emprunter des livres. Veuillez souscrire à un abonnement.");
                    return "redirect:/subscriptions/plans?bookId=" + bookId;
                }
                
                // Check if student can borrow more books based on subscription quota
                if (!subscriptionService.canBorrowBook(user)) {
                    redirectAttributes.addFlashAttribute("error",
                        "Votre quota d'emprunt est épuisé. Veuillez mettre à niveau votre abonnement ou attendre de retourner des livres.");
                    return "redirect:/subscriptions/plans?bookId=" + bookId;
                }
                
                // Ajouter les informations d'abonnement au modèle
                // Initialiser les valeurs par défaut
                model.addAttribute("subscription", null);
                model.addAttribute("subscriptionType", null);
                model.addAttribute("booksRemaining", 0);
                
                // Mettre à jour avec les valeurs réelles si l'abonnement existe
                subscriptionService.getActiveSubscription(user).ifPresent(subscription -> {
                    model.addAttribute("subscription", subscription);
                    model.addAttribute("subscriptionType", subscription.getSubscriptionType());
                    model.addAttribute("booksRemaining", subscription.getBooksRemaining());
                });
                
                // Ajouter les informations de fonds au modèle
                model.addAttribute("walletBalance", user.getBalance());
            }
            
            // Prétraiter les chemins d'image pour s'assurer qu'ils sont corrects
            prepareBookImagePath(book);
            
            model.addAttribute("book", book);
            model.addAttribute("user", user);
            model.addAttribute("expectedReturnDate", LocalDateTime.now().plusDays(14));
            model.addAttribute("currentDate", LocalDateTime.now());
            
            return "books/borrow-confirmation";
            
        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'emprunt: " + e.getMessage());
            return "redirect:/error/borrow";
        }
    }

    @PostMapping("/borrow/{bookId}")
    public String borrowBook(@PathVariable Long bookId,
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        // Vérifier si l'utilisateur est connecté
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("error", "Vous devez être connecté pour emprunter un livre");
            return "redirect:/login";
        }

        try {
            User user = userDetails.getUser();
            
            // Check if user is a student and needs a subscription
            if (user.getUserType() == UserType.STUDENT) {
                // Check if student has an active subscription with remaining quota
                if (!subscriptionService.hasActiveSubscription(user)) {
                    redirectAttributes.addFlashAttribute("error",
                        "Vous devez avoir un abonnement actif pour emprunter des livres. Veuillez souscrire à un abonnement.");
                    return "redirect:/subscriptions/plans?bookId=" + bookId;
                }
                
                // Check if student can borrow more books based on subscription quota
                if (!subscriptionService.canBorrowBook(user)) {
                    redirectAttributes.addFlashAttribute("error",
                        "Votre quota d'emprunt est épuisé. Veuillez mettre à niveau votre abonnement ou attendre de retourner des livres.");
                    return "redirect:/subscriptions/plans?bookId=" + bookId;
                }
            }

            Book book = bookService.getBookById(bookId);
            if (book == null) {
                redirectAttributes.addFlashAttribute("error", "Livre non trouvé");
                return redirectToUserBooks(user);
            }

            // Vérifier si le livre est disponible
            if (book.getAvailableQuantity() <= 0) {
                redirectAttributes.addFlashAttribute("error", "Ce livre n'est pas disponible pour l'emprunt");
                return redirectToUserBooks(user);
            }

            // Vérifier si l'utilisateur a déjà emprunté ce livre
            if (borrowService.hasUserBorrowedBook(user.getId(), bookId)) {
                redirectAttributes.addFlashAttribute("error", "Vous avez déjà emprunté ce livre");
                return redirectToUserBooks(user);
            }

            // Créer l'emprunt
            Borrow borrow = new Borrow();
            borrow.setBook(book);
            borrow.setUser(user);
            borrow.setBorrowDate(LocalDateTime.now());
            // Date de retour prévue (14 jours)
            borrow.setExpectedReturnDate(LocalDateTime.now().plusDays(14));
            borrow.setStatus("EMPRUNTÉ");

            // La quantité sera décrémentée automatiquement dans BorrowingService.createBorrowing()
            // Pas besoin de la décrémenter ici pour éviter la double décrémentation

            // If user is a student, decrement their subscription book quota and handle payment if needed
            if (user.getUserType() == UserType.STUDENT) {
                // Décrémenter le quota de livres de l'abonnement
                subscriptionService.decrementBookQuota(user);
                
                // Vérifier si des frais doivent être appliqués (pour les abonnements non Premium)
                subscriptionService.getActiveSubscription(user).ifPresent(subscription -> {
                    if (subscription.getSubscriptionType() != com.library.enums.SubscriptionType.PREMIUM) {
                        // Pour les abonnements non Premium, on pourrait appliquer des frais supplémentaires
                        // si l'utilisateur dépasse son quota ou pour certains livres spéciaux
                        // Cette logique peut être adaptée selon les besoins
                        
                        // Exemple: déduire 0.50€ pour les frais de service
                        double serviceFee = 0.0; // Mettre à 0.5 pour activer les frais
                        
                        if (serviceFee > 0 && user.getBalance() >= serviceFee) {
                            // Déduire les frais du solde de l'utilisateur
                            user.setBalance(user.getBalance() - serviceFee);
                            // Sauvegarder l'utilisateur avec le nouveau solde
                            // userService.saveUser(user); // Décommenter si nécessaire
                        }
                    }
                });
                
                // Notify student about remaining quota
                int remainingBooks = subscriptionService.getActiveSubscription(user)
                    .map(sub -> sub.getBooksRemaining())
                    .orElse(0);
                
                if (remainingBooks <= 2 && remainingBooks > 0) {
                    notificationService.createSubscriptionQuotaLowNotification(user, remainingBooks);
                }
            }

            // Sauvegarder l'emprunt
            borrowService.saveBorrow(borrow);
            
            // Créer également un enregistrement dans Borrowing pour la compatibilité avec la page des emprunts
            // Variable to store the created borrowing record
            com.library.model.Borrowing createdBorrowing = null;
            
            try {
                createdBorrowing = borrowingService.createBorrowing(user.getId(), book.getId());
                
                // Pour les étudiants, définir le statut comme EN_COURS (emprunt actif)
                // Cela garantit que l'emprunt apparaîtra dans la liste des emprunts en cours
                createdBorrowing.setStatus(BorrowingStatus.EN_COURS);
                createdBorrowing.setBorrowDate(LocalDateTime.now());
                borrowingService.saveBorrowing(createdBorrowing);
            } catch (Exception e) {
                // Log the error but continue, as the Borrow record was already created
                e.printStackTrace();
                System.err.println("Erreur lors de la création de l'enregistrement Borrowing: " + e.getMessage());
                
                // Fall back to the old method if we couldn't create the Borrowing record
                String message = user.getName() + " (" + user.getUserType() + ") a emprunté '" + book.getTitle() + "'.";
                notificationService.createNotificationForRole("LIBRARIAN", message, NotificationType.BOOK_BORROWED);
                
                // Also notify the borrower
                String userMessage = "Vous avez emprunté le livre '" + book.getTitle() + "'.";
                notificationService.createNotification(user, userMessage, NotificationType.BOOK_BORROWED);
            }

            redirectAttributes.addFlashAttribute("success", "Livre emprunté avec succès!");

            // Rediriger selon le rôle
            return redirectToUserBooks(user);

        } catch (Exception e) {
            e.printStackTrace(); // Log the exception
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'emprunt: " + e.getMessage());
            return "redirect:/error/borrow";
        }
    }
    
    /**
     * Prépare le chemin d'image du livre pour l'affichage
     * Cette méthode s'assure que le livre a un chemin d'image valide
     * @param book Le livre dont il faut préparer le chemin d'image
     */
    private void prepareBookImagePath(Book book) {
        // Journaliser les valeurs actuelles pour le débogage
        System.out.println("Préparation du chemin d'image pour le livre ID: " + book.getId());
        System.out.println("coverImageUrl initial: " + book.getCoverImageUrl());
        System.out.println("imagePath initial: " + book.getImagePath());
        
        // Si coverImageUrl est null ou vide, essayer d'utiliser imagePath
        if (book.getCoverImageUrl() == null || book.getCoverImageUrl().trim().isEmpty()) {
            if (book.getImagePath() != null && !book.getImagePath().trim().isEmpty()) {
                book.setCoverImageUrl(book.getImagePath());
                System.out.println("Utilisation de imagePath comme coverImageUrl: " + book.getCoverImageUrl());
            } else {
                // Si les deux sont vides, définir une valeur par défaut
                book.setCoverImageUrl("default-book.jpg");
                System.out.println("Aucun chemin d'image trouvé, utilisation de l'image par défaut");
            }
        }
        
        // Traiter le chemin d'image en fonction de son format
        String coverImageUrl = book.getCoverImageUrl();
        
        // Cas spécial pour les images dans le répertoire /images/
        if (coverImageUrl != null && coverImageUrl.startsWith("/images/")) {
            // Conserver le chemin tel quel pour les images dans /images/
            System.out.println("Image dans le répertoire /images/ détectée, conservation du chemin complet: " + coverImageUrl);
            // On ne modifie pas le chemin, on le garde tel quel
        }
        // Pour les autres chemins, extraire juste le nom du fichier
        else if (coverImageUrl != null && coverImageUrl.contains("/")) {
            String fileName = coverImageUrl.substring(coverImageUrl.lastIndexOf('/') + 1);
            book.setCoverImageUrl(fileName);
            System.out.println("Extraction du nom de fichier à partir du chemin: " + fileName);
        }
        
        // S'assurer que imagePath est également défini pour la cohérence
        if (book.getImagePath() == null || book.getImagePath().trim().isEmpty()) {
            book.setImagePath(book.getCoverImageUrl());
            System.out.println("Mise à jour de imagePath pour correspondre à coverImageUrl: " + book.getImagePath());
        }
        
        System.out.println("Chemin d'image final - coverImageUrl: " + book.getCoverImageUrl() + ", imagePath: " + book.getImagePath());
    }
    
    /**
     * Redirige vers la page des livres appropriée selon le rôle de l'utilisateur
     * @param user L'utilisateur connecté
     * @return La redirection appropriée selon le rôle
     */
    private String redirectToUserBooks(User user) {
        String role = user.getRole();
        if (role.equals("ROLE_STAFF")) {
            return "redirect:/staff/books";
        } else if (role.equals("ROLE_STUDENT")) {
            return "redirect:/student/books";
        } else if (role.equals("ROLE_LIBRARIAN")) {
            return "redirect:/librarian/books";
        } else {
            // Fallback vers le contrôleur général qui redirigera selon le rôle
            return "redirect:/books";
        }
    }
}
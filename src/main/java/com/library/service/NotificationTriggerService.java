package com.library.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.NotificationType;
import com.library.enums.SubscriptionType;
import com.library.enums.UserType;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Reservation;
import com.library.model.User;

/**
 * Service responsible for triggering notifications based on various library actions.
 * This service centralizes all notification creation logic to ensure consistent notification behavior.
 */
@Service
public class NotificationTriggerService {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Trigger notification when a book is borrowed
     */
    @Transactional
    public void onBookBorrowed(Borrowing borrowing) {
        User borrower = borrowing.getUser();
        
        // Notify librarians
        String librarianMessage = borrower.getName() + " (" + borrower.getUserType() + ") a emprunté le livre '"
                + borrowing.getBook().getTitle() + "'.";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.BOOK_BORROWED, null, borrower);
        
        // Notify the borrower
        String userMessage = "Vous avez emprunté le livre '" + borrowing.getBook().getTitle()
                + "'. Date de retour prévue: " + borrowing.getDueDate().toLocalDate() + ".";
        notificationService.createNotification(borrower, userMessage, NotificationType.BOOK_BORROWED, borrower);
    }
    
    /**
     * Trigger notification when a book is returned
     */
    @Transactional
    public void onBookReturned(Borrowing borrowing) {
        User borrower = borrowing.getUser();
        
        // Notify librarians
        String librarianMessage = borrower.getName() + " (" + borrower.getUserType() + ") a retourné le livre '"
                + borrowing.getBook().getTitle() + "'.";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.BOOK_RETURNED, null, borrower);
        
        // Notify the borrower
        String userMessage = "Vous avez retourné le livre '" + borrowing.getBook().getTitle() + "'. Merci!";
        notificationService.createNotification(borrower, userMessage, NotificationType.BOOK_RETURNED, borrower);
    }
    
    /**
     * Trigger notification when a book is overdue
     */
    @Transactional
    public void onBookOverdue(Borrowing borrowing) {
        // Notify librarians
        String librarianMessage = "Le livre '" + borrowing.getBook().getTitle() + "' emprunté par " 
                + borrowing.getUser().getName() + " est en retard (Date de retour: " + borrowing.getDueDate().toLocalDate() + ").";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.OVERDUE_REMINDER, null, borrowing.getUser());
        
        // Notify the borrower
        String userMessage = "Le livre '" + borrowing.getBook().getTitle() 
                + "' est en retard. Veuillez le retourner dès que possible pour éviter des frais supplémentaires.";
        notificationService.createNotification(borrowing.getUser(), userMessage, NotificationType.OVERDUE_REMINDER, borrowing.getUser());
    }
    
    /**
     * Trigger notification when a book is due soon (within the next few days)
     */
    @Transactional
    public void onBookDueSoon(Borrowing borrowing) {
        // Notify the borrower
        String userMessage = "Le livre '" + borrowing.getBook().getTitle()
                + "' doit être retourné bientôt (Date de retour: " + borrowing.getDueDate().toLocalDate() + ").";
        notificationService.createNotification(borrowing.getUser(), userMessage, NotificationType.DUE_SOON_REMINDER, borrowing.getUser());
        
        // Notify librarians as well
        String librarianMessage = "Le livre '" + borrowing.getBook().getTitle() + "' emprunté par "
                + borrowing.getUser().getName() + " sera dû bientôt (Date de retour: " + borrowing.getDueDate().toLocalDate() + ").";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.DUE_SOON_REMINDER, null, borrowing.getUser());
    }
    
    /**
     * Trigger notification when a new book is added to the library
     */
    @Transactional
    public void onNewBookAdded(Book book, User librarian) {
        // Notify all users except the librarian who added the book
        String message = "Nouveau livre ajouté à la bibliothèque: '" + book.getTitle() + "' par " + book.getAuthor() + ".";
        
        // Notify students
        notificationService.createNotificationForUserType(UserType.STUDENT, message, NotificationType.NEW_BOOK_ADDED, null, librarian);
        
        // Notify staff
        notificationService.createNotificationForUserType(UserType.STAFF, message, NotificationType.NEW_BOOK_ADDED, null, librarian);
        
        // Notify other librarians
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, message, NotificationType.NEW_BOOK_ADDED, librarian, librarian);
    }
    
    /**
     * Trigger notification when a book is updated
     */
    @Transactional
    public void onBookUpdated(Book book, User librarian) {
        // Notify librarians only
        String message = "Le livre '" + book.getTitle() + "' a été mis à jour.";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, message, NotificationType.BOOK_UPDATE, librarian, librarian);
    }
    
    /**
     * Trigger notification when a book's stock is running low
     */
    @Transactional
    public void onLowBookStock(Book book) {
        // Notify librarians only
        String message = "Stock bas pour le livre '" + book.getTitle() 
                + "'. Quantité disponible: " + book.getAvailableQuantity() + ".";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, message, NotificationType.LOW_BOOK_STOCK, null, null);
    }
    
    /**
     * Trigger notification when a book is damaged
     */
    @Transactional
    public void onBookDamageReported(Book book, User reporter, String damageDescription) {
        // Notify librarians
        String message = "Dommage signalé pour le livre '" + book.getTitle() 
                + "' par " + reporter.getName() + ": " + damageDescription;
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, message, NotificationType.BOOK_DAMAGE_REPORTED, null, reporter);
    }
    
    /**
     * Trigger notification when a book is lost
     */
    @Transactional
    public void onBookLostReported(Borrowing borrowing) {
        // Notify librarians
        String librarianMessage = "Livre perdu: '" + borrowing.getBook().getTitle() 
                + "' par " + borrowing.getUser().getName() + ".";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.BOOK_LOST_REPORTED, null, borrowing.getUser());
        
        // Notify the user
        String userMessage = "Vous avez signalé la perte du livre '" + borrowing.getBook().getTitle() 
                + "'. Des frais peuvent s'appliquer.";
        notificationService.createNotification(borrowing.getUser(), userMessage, NotificationType.BOOK_LOST_REPORTED, borrowing.getUser());
    }
    
    /**
     * Trigger notification when a fine is added to a user's account
     */
    @Transactional
    public void onFineAdded(User user, double amount, String reason) {
        // Notify librarians
        String librarianMessage = "Amende ajoutée pour " + user.getName() 
                + ": " + amount + " € (" + reason + ").";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.FINE_ADDED, null, null);
        
        // Notify the user
        String userMessage = "Une amende de " + amount + " € a été ajoutée à votre compte (" + reason + ").";
        notificationService.createNotification(user, userMessage, NotificationType.FINE_ADDED, null);
    }
    
    /**
     * Trigger notification when a fine is paid
     */
    @Transactional
    public void onFinePaid(User user, double amount) {
        // Notify librarians
        String librarianMessage = user.getName() + " a payé une amende de " + amount + " €.";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.FINE_PAID, null, user);
        
        // Notify the user
        String userMessage = "Votre paiement de " + amount + " € a été reçu. Merci!";
        notificationService.createNotification(user, userMessage, NotificationType.FINE_PAID, user);
    }
    
    /**
     * Trigger notification when a book is reserved
     */
    @Transactional
    public void onBookReserved(Reservation reservation) {
        // Notify librarians
        String librarianMessage = reservation.getUser().getName() + " a réservé le livre '" 
                + reservation.getBook().getTitle() + "'.";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.RESERVATION_CONFIRMED, null, reservation.getUser());
        
        // Notify the user
        String userMessage = "Vous avez réservé le livre '" + reservation.getBook().getTitle() 
                + "'. Vous serez notifié lorsqu'il sera disponible.";
        notificationService.createNotification(reservation.getUser(), userMessage, NotificationType.RESERVATION_CONFIRMED, reservation.getUser());
    }
    
    /**
     * Trigger notification when a reserved book becomes available
     */
    @Transactional
    public void onReservationAvailable(Reservation reservation) {
        // Notify the user
        String userMessage = "Le livre que vous avez réservé '" + reservation.getBook().getTitle() 
                + "' est maintenant disponible. Veuillez le récupérer dans les 48 heures.";
        notificationService.createNotification(reservation.getUser(), userMessage, NotificationType.RESERVATION_AVAILABLE, null);
    }
    
    /**
     * Trigger notification when a reservation is cancelled
     */
    @Transactional
    public void onReservationCancelled(Reservation reservation, boolean byUser) {
        if (byUser) {
            // Cancelled by the user
            String librarianMessage = reservation.getUser().getName() + " a annulé sa réservation pour le livre '" 
                    + reservation.getBook().getTitle() + "'.";
            notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.RESERVATION_CANCELLED, null, reservation.getUser());
        } else {
            // Cancelled by a librarian or the system
            String userMessage = "Votre réservation pour le livre '" + reservation.getBook().getTitle() 
                    + "' a été annulée.";
            notificationService.createNotification(reservation.getUser(), userMessage, NotificationType.RESERVATION_CANCELLED, null);
        }
    }
    
    /**
     * Trigger notification when a reservation expires
     */
    @Transactional
    public void onReservationExpired(Reservation reservation) {
        // Notify librarians
        String librarianMessage = "La réservation de " + reservation.getUser().getName() + " pour le livre '" 
                + reservation.getBook().getTitle() + "' a expiré.";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.RESERVATION_EXPIRED, null, reservation.getUser());
        
        // Notify the user
        String userMessage = "Votre réservation pour le livre '" + reservation.getBook().getTitle() 
                + "' a expiré car vous ne l'avez pas récupéré dans le délai imparti.";
        notificationService.createNotification(reservation.getUser(), userMessage, NotificationType.RESERVATION_EXPIRED, reservation.getUser());
    }
    
    /**
     * Trigger notification when a new member registers
     */
    @Transactional
    public void onNewMemberRegistered(User newUser) {
        // Notify librarians
        String message = "Nouveau membre inscrit: " + newUser.getName() + " (" + newUser.getUserType() + ").";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, message, NotificationType.NEW_MEMBER_REGISTERED, null, newUser);
    }
    
    /**
     * Trigger notification when a book renewal is requested
     */
    @Transactional
    public void onBookRenewalRequested(Borrowing borrowing) {
        // Notify librarians
        String message = borrowing.getUser().getName() + " a demandé le renouvellement du livre '" 
                + borrowing.getBook().getTitle() + "'.";
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, message, NotificationType.APPROVAL_REQUIRED, null, borrowing.getUser());
    }
    
    /**
     * Trigger notification when a book renewal is confirmed
     */
    @Transactional
    public void onBookRenewalConfirmed(Borrowing borrowing) {
        // Notify the user
        String message = "Votre demande de renouvellement pour le livre '" + borrowing.getBook().getTitle() 
                + "' a été acceptée. Nouvelle date de retour: " + borrowing.getDueDate().toLocalDate() + ".";
        notificationService.createNotification(borrowing.getUser(), message, NotificationType.BOOK_RENEWAL_CONFIRMED, null);
    }
    
    /**
     * Trigger notification when a book renewal is rejected
     */
    @Transactional
    public void onBookRenewalRejected(Borrowing borrowing, String reason) {
        // Notify the user
        String message = "Votre demande de renouvellement pour le livre '" + borrowing.getBook().getTitle() 
                + "' a été rejetée" + (reason != null ? " : " + reason : ".") 
                + " Date de retour actuelle: " + borrowing.getDueDate().toLocalDate() + ".";
        notificationService.createNotification(borrowing.getUser(), message, NotificationType.BOOK_RENEWAL_REJECTED, null);
    }
    
    /**
     * Trigger notification when a task is assigned to a staff member
     */
    @Transactional
    public void onTaskAssigned(User staff, String taskDescription) {
        // Notify the staff member
        String message = "Nouvelle tâche assignée: " + taskDescription;
        notificationService.createNotification(staff, message, NotificationType.TASK_ASSIGNED, null);
    }
    
    /**
     * Trigger notification for a general announcement to all users
     */
    @Transactional
    public void sendGeneralAnnouncement(String message) {
        // Notify all users
        notificationService.createNotificationForUserType(UserType.STUDENT, message, NotificationType.GENERAL_MESSAGE, null, null);
        notificationService.createNotificationForUserType(UserType.STAFF, message, NotificationType.GENERAL_MESSAGE, null, null);
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, message, NotificationType.GENERAL_MESSAGE, null, null);
    }
    
    /**
     * Trigger notification for a role-specific announcement
     */
    @Transactional
    public void sendRoleAnnouncement(UserType userType, String message) {
        notificationService.createNotificationForUserType(userType, message, NotificationType.GENERAL_MESSAGE, null, null);
    }
    
    /**
     * Trigger notification for book recommendations to a specific user
     */
    @Transactional
    public void sendBookRecommendation(User user, Book book, String reason) {
        String message = "Recommandation de livre: '" + book.getTitle() + "' par " + book.getAuthor() +
                (reason != null ? ". " + reason : "");
        notificationService.createNotification(user, message, NotificationType.BOOK_RECOMMENDATION, null);
    }
    
    /**
     * Trigger notification for book recommendations to users of a specific type
     */
    @Transactional
    public void sendBookRecommendationByUserType(UserType userType, Book book, String reason) {
        String message = "Recommandation de livre: '" + book.getTitle() + "' par " + book.getAuthor() +
                (reason != null ? ". " + reason : "");
        notificationService.createNotificationForUserType(userType, message, NotificationType.BOOK_RECOMMENDATION, null, null);
    }
    
    /**
     * Trigger notification when a student's account status changes
     */
    @Transactional
    public void onAccountStatusChange(User user, String oldStatus, String newStatus, String reason) {
        // Notify librarians
        String librarianMessage = "Statut du compte de " + user.getName() + " modifié: " +
                oldStatus + " → " + newStatus + (reason != null ? " (" + reason + ")" : "");
        notificationService.createNotificationForUserType(UserType.LIBRARIAN, librarianMessage, NotificationType.ACCOUNT_STATUS_CHANGE, null, null);
        
        // Notify the user
        String userMessage = "Le statut de votre compte a été modifié: " + oldStatus + " → " + newStatus +
                (reason != null ? ". Raison: " + reason : "");
        notificationService.createNotification(user, userMessage, NotificationType.ACCOUNT_STATUS_CHANGE, null);
    }
    
    /**
     * Trigger notification when funds are added to a user's account
     */
    @Transactional
    public void onFundsAdded(User user, double amount) {
        // Notify the user
        String message = String.format("%.0f Ar has been added to your account balance.", amount);
        notificationService.createNotification(user, message, NotificationType.GENERAL_MESSAGE, null);
    }
    
    /**
     * Trigger notification when a subscription is extended
     */
    @Transactional
    public void onSubscriptionExtended(User user, int additionalDays) {
        // Notify the user
        String message = "Votre abonnement a été prolongé de " + additionalDays + " jours par un bibliothécaire.";
        notificationService.createNotification(user, message, NotificationType.SUBSCRIPTION_RENEWED, null);
    }
    
    /**
     * Trigger notification when books are added to a subscription quota
     */
    @Transactional
    public void onBooksAddedToQuota(User user, int additionalBooks) {
        // Notify the user
        String message = additionalBooks + " livre(s) ont été ajoutés à votre quota d'abonnement par un bibliothécaire.";
        notificationService.createNotification(user, message, NotificationType.SUBSCRIPTION_RENEWED, null);
    }
    
    /**
     * Trigger notification when a subscription is purchased
     */
    @Transactional
    public void onSubscriptionPurchased(User user, SubscriptionType subscriptionType) {
        notificationService.createSubscriptionPurchaseNotification(user, subscriptionType);
    }
    
    /**
     * Trigger notification when a subscription is cancelled
     */
    @Transactional
    public void onSubscriptionCancelled(User user) {
        notificationService.createSubscriptionCancellationNotification(user);
    }
}
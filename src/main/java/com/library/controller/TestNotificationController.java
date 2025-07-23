package com.library.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.library.enums.NotificationType;
import com.library.enums.UserType;
import com.library.model.User;
import com.library.service.NotificationService;
import com.library.service.UserService;

/**
 * Contrôleur temporaire pour tester les notifications avec photos de profil
 */
@Controller
@RequestMapping("/test")
@PreAuthorize("hasRole('LIBRARIAN')")
public class TestNotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Créer des notifications de test avec différents utilisateurs déclencheurs
     */
    @GetMapping("/create-test-notifications")
    @ResponseBody
    public String createTestNotifications() {
        try {
            // Récupérer quelques utilisateurs pour les tests
            User librarian = userService.findByUserType(UserType.LIBRARIAN).stream().findFirst().orElse(null);
            User student = userService.findByUserType(UserType.STUDENT).stream().findFirst().orElse(null);
            User staff = userService.findByUserType(UserType.STAFF).stream().findFirst().orElse(null);
            
            if (librarian == null || student == null) {
                return "Erreur: Impossible de trouver les utilisateurs nécessaires pour les tests";
            }
            
            // Test 1: Notification déclenchée par un étudiant (emprunt de livre)
            if (student != null) {
                notificationService.createNotificationForUserType(
                    UserType.LIBRARIAN, 
                    "TEST: " + student.getName() + " (STUDENT) a emprunté le livre 'Test Book 1'.", 
                    NotificationType.BOOK_BORROWED, 
                    null, 
                    student
                );
                
                notificationService.createNotification(
                    student, 
                    "TEST: Vous avez emprunté le livre 'Test Book 1'.", 
                    NotificationType.BOOK_BORROWED, 
                    student
                );
            }
            
            // Test 2: Notification déclenchée par un bibliothécaire (nouveau livre)
            if (librarian != null) {
                notificationService.createNotificationForUserType(
                    UserType.STUDENT, 
                    "TEST: Nouveau livre ajouté par " + librarian.getName() + " (LIBRARIAN): 'Test Book 2'.", 
                    NotificationType.NEW_BOOK_ADDED, 
                    null, 
                    librarian
                );
                
                notificationService.createNotificationForUserType(
                    UserType.STAFF, 
                    "TEST: Nouveau livre ajouté par " + librarian.getName() + " (LIBRARIAN): 'Test Book 2'.", 
                    NotificationType.NEW_BOOK_ADDED, 
                    null, 
                    librarian
                );
            }
            
            // Test 3: Notification déclenchée par un staff (retour de livre)
            if (staff != null) {
                notificationService.createNotificationForUserType(
                    UserType.LIBRARIAN, 
                    "TEST: " + staff.getName() + " (STAFF) a retourné le livre 'Test Book 3'.", 
                    NotificationType.BOOK_RETURNED, 
                    null, 
                    staff
                );
                
                notificationService.createNotification(
                    staff, 
                    "TEST: Vous avez retourné le livre 'Test Book 3'.", 
                    NotificationType.BOOK_RETURNED, 
                    staff
                );
            }
            
            // Test 4: Notification système (sans utilisateur déclencheur)
            notificationService.createNotificationForUserType(
                UserType.LIBRARIAN, 
                "TEST: Notification système - Maintenance programmée ce soir.", 
                NotificationType.GENERAL_MESSAGE, 
                null, 
                null
            );
            
            return "✅ Notifications de test créées avec succès ! " +
                   "Vérifiez le panneau de notifications pour voir les photos de profil.";
                   
        } catch (Exception e) {
            return "❌ Erreur lors de la création des notifications de test: " + e.getMessage();
        }
    }
    
    /**
     * Supprimer les notifications de test
     */
    @GetMapping("/cleanup-test-notifications")
    @ResponseBody
    public String cleanupTestNotifications() {
        try {
            // Note: Cette méthode nécessiterait une implémentation plus sophistiquée
            // pour supprimer seulement les notifications de test
            return "⚠️ Fonction de nettoyage non implémentée. " +
                   "Les notifications de test resteront dans le système.";
        } catch (Exception e) {
            return "❌ Erreur lors du nettoyage: " + e.getMessage();
        }
    }
}
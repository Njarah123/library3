package com.library.controller;

import com.library.model.Notification;
import com.library.model.User;
import com.library.service.NotificationService;
import com.library.service.UserService;
import com.library.security.CustomUserDetails;
import com.library.dto.NotificationDTO;
import com.library.dto.NotificationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationRestController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    /**
     * Récupérer toutes les notifications de l'utilisateur connecté
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(Authentication authentication) {
        System.out.println("🔍 DEBUG: Début de getNotifications()");
        
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            System.out.println("🔍 DEBUG: Utilisateur connecté: " + user.getUsername() + " (ID: " + user.getId() + ")");
            
            List<Notification> notifications = notificationService.getNotificationsByUser(user);
            System.out.println("🔍 DEBUG: " + (notifications != null ? notifications.size() : 0) + " notifications trouvées");
            
            // DEBUG: Créer des notifications de test si aucune n'existe
            if (notifications == null || notifications.isEmpty()) {
                System.out.println("🔍 DEBUG: Aucune notification trouvée, création d'une notification de test");
                
                try {
                    Notification testNotification = notificationService.createNotification(
                        user,
                        "Notification de test - Bienvenue dans le système!",
                        com.library.enums.NotificationType.GENERAL_MESSAGE
                    );
                    notifications = java.util.Arrays.asList(testNotification);
                    System.out.println("✅ DEBUG: Notification de test créée avec succès");
                } catch (Exception e) {
                    System.out.println("❌ DEBUG: Erreur lors de la création de la notification de test: " + e.getMessage());
                    notifications = new ArrayList<>();
                }
            }
            
            long unreadCount = notificationService.getUnreadNotificationCount(user);
            System.out.println("🔍 DEBUG: Nombre de notifications non lues: " + unreadCount);
            
            // Créer une réponse JSON ultra-simple
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"success\":true,");
            jsonResponse.append("\"unreadCount\":").append(unreadCount).append(",");
            jsonResponse.append("\"notifications\":[");
            
            for (int i = 0; i < notifications.size(); i++) {
                Notification notification = notifications.get(i);
                if (i > 0) jsonResponse.append(",");
                
                jsonResponse.append("{");
                jsonResponse.append("\"id\":").append(notification.getId()).append(",");
                jsonResponse.append("\"message\":\"").append(notification.getMessage().replace("\"", "\\\"")).append("\",");
                jsonResponse.append("\"type\":\"").append(notification.getType() != null ? notification.getType().toString() : "GENERAL_MESSAGE").append("\",");
                jsonResponse.append("\"read\":").append(notification.isRead()).append(",");
                
                // Informations de l'utilisateur déclencheur (pour la photo de profil)
                if (notification.getTriggeredByUser() != null) {
                    User triggeredBy = notification.getTriggeredByUser();
                    jsonResponse.append("\"triggeredByUser\":{");
                    jsonResponse.append("\"id\":").append(triggeredBy.getId()).append(",");
                    jsonResponse.append("\"name\":\"").append(triggeredBy.getName().replace("\"", "\\\"")).append("\",");
                    jsonResponse.append("\"username\":\"").append(triggeredBy.getUsername().replace("\"", "\\\"")).append("\",");
                    jsonResponse.append("\"userType\":\"").append(triggeredBy.getUserType().toString()).append("\",");
                    
                    // Photo de profil
                    String profileImagePath = triggeredBy.getProfileImagePath();
                    if (profileImagePath != null && !profileImagePath.trim().isEmpty()) {
                        jsonResponse.append("\"profileImagePath\":\"").append(profileImagePath.replace("\"", "\\\"")).append("\"");
                    } else {
                        // Image par défaut basée sur le type d'utilisateur
                        String defaultImage = "/images/default-" + triggeredBy.getUserType().toString().toLowerCase() + ".png";
                        jsonResponse.append("\"profileImagePath\":\"").append(defaultImage).append("\"");
                    }
                    
                    jsonResponse.append("},");
                } else {
                    jsonResponse.append("\"triggeredByUser\":null,");
                }
                
                // Date
                if (notification.getCreatedAt() != null) {
                    jsonResponse.append("\"createdAt\":\"").append(notification.getCreatedAt().toString()).append("\"");
                } else if (notification.getTimestamp() != null) {
                    jsonResponse.append("\"createdAt\":\"").append(notification.getTimestamp().toString()).append("\"");
                } else {
                    jsonResponse.append("\"createdAt\":\"").append(java.time.LocalDateTime.now().toString()).append("\"");
                }
                
                jsonResponse.append("}");
            }
            
            jsonResponse.append("],");
            jsonResponse.append("\"timestamp\":").append(System.currentTimeMillis());
            jsonResponse.append("}");
            
            System.out.println("✅ DEBUG: JSON Response créé: " + jsonResponse.toString());
            
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(jsonResponse.toString());
                
        } catch (Exception e) {
            System.out.println("❌ DEBUG: Erreur dans getNotifications(): " + e.getMessage());
            e.printStackTrace();
            
            String errorJson = "{\"success\":false,\"message\":\"Erreur: " + e.getMessage().replace("\"", "\\\"") + "\",\"notifications\":[],\"unreadCount\":0}";
            return ResponseEntity.ok()
                .header("Content-Type", "application/json")
                .body(errorJson);
        }
    }

    /**
     * Obtenir le nombre de notifications non lues
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            long unreadCount = notificationService.getUnreadNotificationCount(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", unreadCount);
            response.put("notifications", new ArrayList<>());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération du compteur: " + e.getMessage());
            errorResponse.put("unreadCount", 0);
            errorResponse.put("notifications", new ArrayList<>());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Marquer une notification comme lue
     */
    @PostMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            boolean success = notificationService.markAsRead(id, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("timestamp", System.currentTimeMillis());
            
            if (success) {
                long unreadCount = notificationService.getUnreadNotificationCount(user);
                response.put("unreadCount", unreadCount);
                response.put("message", "Notification marquée comme lue");
            } else {
                response.put("message", "Notification non trouvée ou non autorisée");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors du marquage: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Marquer toutes les notifications comme lues
     */
    @PostMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            int markedCount = notificationService.markAllAsRead(user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("markedCount", markedCount);
            response.put("unreadCount", 0);
            response.put("message", markedCount + " notifications marquées comme lues");
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors du marquage: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Supprimer une notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id, Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            boolean success = notificationService.deleteNotification(id, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("timestamp", System.currentTimeMillis());
            
            if (success) {
                long unreadCount = notificationService.getUnreadNotificationCount(user);
                response.put("unreadCount", unreadCount);
                response.put("message", "Notification supprimée");
            } else {
                response.put("message", "Notification non trouvée ou non autorisée");
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la suppression: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Obtenir les dernières notifications (limitées)
     */
    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentNotifications(
            @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            List<Notification> notifications = notificationService.getLatestNotifications(user, limit);
            long unreadCount = notificationService.getUnreadNotificationCount(user);
            
            // Convertir les notifications en format JSON simple
            List<Map<String, Object>> notificationList = new java.util.ArrayList<>();
            for (Notification notification : notifications) {
                notificationList.add(convertNotificationToMap(notification));
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("notifications", notificationList);
            response.put("unreadCount", unreadCount);
            response.put("success", true);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la récupération: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Vérifier s'il y a de nouvelles notifications
     */
    @GetMapping("/check-new")
    public ResponseEntity<Map<String, Object>> checkNewNotifications(
            @RequestParam(required = false) Long lastCheckTimestamp,
            Authentication authentication) {
        try {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetails.getUser();
            
            long unreadCount = notificationService.getUnreadNotificationCount(user);
            boolean hasNew = false;
            
            if (lastCheckTimestamp != null) {
                // Logique pour vérifier s'il y a de nouvelles notifications depuis le dernier check
                // Pour simplifier, on considère qu'il y a du nouveau si unreadCount > 0
                hasNew = unreadCount > 0;
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("unreadCount", unreadCount);
            response.put("hasNew", hasNew);
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Erreur lors de la vérification: " + e.getMessage());
            errorResponse.put("timestamp", System.currentTimeMillis());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    /**
     * Convertir une notification en Map pour JSON
     */
    private Map<String, Object> convertNotificationToMap(Notification notification) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", notification.getId());
        map.put("message", notification.getMessage());
        map.put("type", notification.getType().name());
        map.put("read", notification.isRead());
        
        // Informations de l'utilisateur déclencheur (pour la photo de profil)
        if (notification.getTriggeredByUser() != null) {
            User triggeredBy = notification.getTriggeredByUser();
            Map<String, Object> triggeredByMap = new HashMap<>();
            triggeredByMap.put("id", triggeredBy.getId());
            triggeredByMap.put("name", triggeredBy.getName());
            triggeredByMap.put("username", triggeredBy.getUsername());
            triggeredByMap.put("userType", triggeredBy.getUserType().toString());
            
            // Photo de profil
            String profileImagePath = triggeredBy.getProfileImagePath();
            if (profileImagePath != null && !profileImagePath.trim().isEmpty()) {
                triggeredByMap.put("profileImagePath", profileImagePath);
            } else {
                // Image par défaut basée sur le type d'utilisateur
                String defaultImage = "/images/default-" + triggeredBy.getUserType().toString().toLowerCase() + ".png";
                triggeredByMap.put("profileImagePath", defaultImage);
            }
            
            map.put("triggeredByUser", triggeredByMap);
        } else {
            map.put("triggeredByUser", null);
        }
        
        // Formater la date
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        if (notification.getCreatedAt() != null) {
            map.put("createdAt", notification.getCreatedAt().format(formatter));
            map.put("timestamp", notification.getCreatedAt().toString());
        } else if (notification.getTimestamp() != null) {
            map.put("createdAt", notification.getTimestamp().format(formatter));
            map.put("timestamp", notification.getTimestamp().toString());
        }
        
        return map;
    }
}
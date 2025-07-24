package com.library.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.library.model.User;
import com.library.service.UserService;

@RestController
@RequestMapping("/api/users")
public class ProfileImageController {
    
    @Autowired
    private UserService userService;
    
    /**
     * Endpoint pour servir les images de profil stockées en base de données
     * @param userId L'ID de l'utilisateur
     * @return L'image de profil en tant que ResponseEntity
     */
    @GetMapping("/{userId}/profile-image")
    public ResponseEntity<byte[]> getProfileImage(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            
            // Vérifier si l'utilisateur a une image de profil
            if (!user.hasProfileImage()) {
                // Retourner une image par défaut ou une erreur 404
                return ResponseEntity.notFound().build();
            }
            
            // Préparer les headers HTTP
            HttpHeaders headers = new HttpHeaders();
            
            // Définir le type de contenu
            String contentType = user.getProfileImageContentType();
            if (contentType != null && !contentType.isEmpty()) {
                headers.setContentType(MediaType.parseMediaType(contentType));
            } else {
                // Type par défaut si non spécifié
                headers.setContentType(MediaType.IMAGE_JPEG);
            }
            
            // Définir la taille du contenu
            headers.setContentLength(user.getProfileImageData().length);
            
            // Ajouter des headers pour le cache (optionnel)
            headers.setCacheControl("max-age=3600"); // Cache pendant 1 heure
            
            return new ResponseEntity<>(user.getProfileImageData(), headers, HttpStatus.OK);
            
        } catch (RuntimeException e) {
            // Utilisateur non trouvé
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            // Erreur interne
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Endpoint pour vérifier si un utilisateur a une image de profil
     * @param userId L'ID de l'utilisateur
     * @return true si l'utilisateur a une image, false sinon
     */
    @GetMapping("/{userId}/has-profile-image")
    public ResponseEntity<Boolean> hasProfileImage(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user.hasProfileImage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
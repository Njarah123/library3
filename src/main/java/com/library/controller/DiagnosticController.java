package com.library.controller;

import com.library.dto.UserRegistrationDto;
import com.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Contrôleur de diagnostic pour résoudre les problèmes d'inscription
 * Ce contrôleur fournit des endpoints de test pour contourner les erreurs environnementales
 */
@Controller
@RequestMapping("/diagnostic")
public class DiagnosticController {

    private static final Logger logger = LoggerFactory.getLogger(DiagnosticController.class);

    @Autowired
    private UserService userService;

    @Value("${upload.path}")
    private String uploadPath;

    /**
     * Page de diagnostic pour tester l'inscription
     */
    @GetMapping("/register")
    public String showDiagnosticRegister(Model model) {
        logger.info("=== DIAGNOSTIC: Page d'inscription de test ===");
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        model.addAttribute("diagnosticMode", true);
        return "diagnostic/register";
    }

    /**
     * Test d'inscription sans multipart (pour éviter FileCountLimitExceededException)
     */
    @PostMapping("/register-no-multipart")
    public String registerWithoutMultipart(@ModelAttribute("user") UserRegistrationDto user,
                                         BindingResult result,
                                         Model model,
                                         RedirectAttributes redirectAttributes,
                                         HttpServletRequest request) {
        try {
            logger.info("=== DIAGNOSTIC: Inscription sans multipart ===");
            logger.info("Username: {}", user.getUsername());
            logger.info("Email: {}", user.getEmail());
            logger.info("UserType: {}", user.getUserType());
            
            // Créer une session explicitement
            HttpSession session = request.getSession(true);
            logger.info("Session ID: {}", session.getId());
            
            // Validation basique
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                result.rejectValue("username", "error.user", "Le nom d'utilisateur est obligatoire");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                result.rejectValue("password", "error.user", "Le mot de passe est obligatoire");
            }
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                result.rejectValue("name", "error.user", "Le nom est obligatoire");
            }

            if (result.hasErrors()) {
                logger.error("Erreurs de validation: {}", result.getAllErrors());
                model.addAttribute("diagnosticMode", true);
                return "diagnostic/register";
            }

            // Utiliser l'image par défaut selon le type d'utilisateur
            String defaultImage = getDefaultImageForUserType(user.getUserType());
            user.setProfileImagePath(defaultImage);
            logger.info("Image par défaut utilisée: {}", defaultImage);

            // Inscription
            userService.registerNewUser(user);
            logger.info("=== DIAGNOSTIC: Inscription réussie sans multipart ===");
            
            redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie (mode diagnostic) ! Vous pouvez maintenant vous connecter.");
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            logger.error("=== DIAGNOSTIC: Erreur inscription sans multipart ===", e);
            model.addAttribute("errorMessage", "Erreur lors de l'inscription (mode diagnostic): " + e.getMessage());
            model.addAttribute("diagnosticMode", true);
            return "diagnostic/register";
        }
    }

    /**
     * Test d'inscription avec multipart mais gestion d'erreur très robuste
     */
    @PostMapping("/register-with-multipart")
    public String registerWithMultipart(@ModelAttribute("user") UserRegistrationDto user,
                                      BindingResult result,
                                      @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                      Model model,
                                      RedirectAttributes redirectAttributes,
                                      HttpServletRequest request) {
        try {
            logger.info("=== DIAGNOSTIC: Inscription avec multipart ===");
            logger.info("Username: {}", user.getUsername());
            logger.info("Email: {}", user.getEmail());
            logger.info("UserType: {}", user.getUserType());
            logger.info("Image reçue: {}", profileImage != null ? profileImage.getOriginalFilename() : "Aucune");
            
            // Créer une session explicitement
            HttpSession session = request.getSession(true);
            logger.info("Session ID: {}", session.getId());
            
            // Validation basique
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                result.rejectValue("username", "error.user", "Le nom d'utilisateur est obligatoire");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                result.rejectValue("password", "error.user", "Le mot de passe est obligatoire");
            }
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                result.rejectValue("name", "error.user", "Le nom est obligatoire");
            }

            if (result.hasErrors()) {
                logger.error("Erreurs de validation: {}", result.getAllErrors());
                model.addAttribute("diagnosticMode", true);
                return "diagnostic/register";
            }

            String imagePath;
            
            // Gestion très robuste de l'upload d'image
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    logger.info("Tentative d'upload d'image...");
                    imagePath = handleProfileImageUploadSafe(profileImage);
                    logger.info("Image uploadée avec succès: {}", imagePath);
                } catch (Exception e) {
                    logger.warn("Erreur lors de l'upload d'image, utilisation de l'image par défaut: ", e);
                    imagePath = getDefaultImageForUserType(user.getUserType());
                }
            } else {
                imagePath = getDefaultImageForUserType(user.getUserType());
                logger.info("Aucune image fournie, utilisation de l'image par défaut: {}", imagePath);
            }
            
            user.setProfileImagePath(imagePath);

            // Inscription
            userService.registerNewUser(user);
            logger.info("=== DIAGNOSTIC: Inscription réussie avec multipart ===");
            
            redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie avec photo (mode diagnostic) ! Vous pouvez maintenant vous connecter.");
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            logger.error("=== DIAGNOSTIC: Erreur inscription avec multipart ===", e);
            model.addAttribute("errorMessage", "Erreur lors de l'inscription avec photo (mode diagnostic): " + e.getMessage());
            model.addAttribute("diagnosticMode", true);
            return "diagnostic/register";
        }
    }

    /**
     * Endpoint de test pour vérifier la configuration multipart
     */
    @GetMapping("/multipart-test")
    @ResponseBody
    public String testMultipartConfig() {
        StringBuilder info = new StringBuilder();
        info.append("=== DIAGNOSTIC MULTIPART ===\n");
        info.append("FileCountMax: ").append(System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileCountMax")).append("\n");
        info.append("FileSizeMax: ").append(System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.fileSizeMax")).append("\n");
        info.append("SizeMax: ").append(System.getProperty("org.apache.tomcat.util.http.fileupload.FileUploadBase.sizeMax")).append("\n");
        info.append("Upload Path: ").append(uploadPath).append("\n");
        
        // Vérifier si le répertoire d'upload existe
        Path uploadDir = Paths.get(uploadPath, "profiles");
        info.append("Upload Directory Exists: ").append(Files.exists(uploadDir)).append("\n");
        info.append("Upload Directory Path: ").append(uploadDir.toAbsolutePath()).append("\n");
        
        return info.toString();
    }

    /**
     * Gestion sécurisée de l'upload d'image avec gestion d'erreur complète
     */
    private String handleProfileImageUploadSafe(MultipartFile profileImage) throws IOException {
        try {
            // Vérification du type de fichier
            String contentType = profileImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Le fichier doit être une image");
            }
            
            // Vérification de la taille
            if (profileImage.getSize() > 50 * 1024 * 1024) { // 50MB
                throw new IllegalArgumentException("Le fichier est trop volumineux (max 50MB)");
            }
            
            // Création du répertoire d'upload
            Path uploadDir = Paths.get(uploadPath, "profiles");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                logger.info("Répertoire d'upload créé: {}", uploadDir.toAbsolutePath());
            }

            // Génération du nom de fichier unique
            String originalFilename = profileImage.getOriginalFilename();
            String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;

            // Sauvegarde du fichier
            Path filePath = uploadDir.resolve(filename);
            Files.copy(profileImage.getInputStream(), filePath);
            
            logger.info("Fichier sauvegardé: {}", filePath.toAbsolutePath());
            return "/uploads/profiles/" + filename;
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'upload d'image: ", e);
            throw new IOException("Erreur lors de l'upload d'image: " + e.getMessage(), e);
        }
    }
    
    /**
     * Retourne l'image par défaut selon le type d'utilisateur
     */
    private String getDefaultImageForUserType(String userType) {
        if (userType == null) {
            return "/images/default-student.png";
        }
        
        switch (userType.toUpperCase()) {
            case "STUDENT":
                return "/images/default-student.png";
            case "STAFF":
                return "/images/default-staff.png";
            case "LIBRARIAN":
                return "/images/default-librarian.png";
            default:
                return "/images/default-student.png";
        }
    }
}
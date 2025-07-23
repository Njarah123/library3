package com.library.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.dto.UserRegistrationDto;
import com.library.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/login")
    public String login(Authentication authentication, HttpServletRequest request, Model model) {
        // S'assurer que la session est créée immédiatement
        HttpSession session = request.getSession(true);
        
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            String role = authentication.getAuthorities().iterator().next().getAuthority();
            switch (role) {
                case "ROLE_LIBRARIAN": return "redirect:/librarian/dashboard";
                case "ROLE_STAFF": return "redirect:/staff/dashboard";
                case "ROLE_STUDENT": return "redirect:/student/dashboard";
                default: return "redirect:/dashboard";
            }
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remembered-users".equals(cookie.getName())) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String decodedValue = new String(Base64.getUrlDecoder().decode(cookie.getValue()));
                        List<Map<String, String>> rememberedUsers = mapper.readValue(decodedValue, new TypeReference<List<Map<String, String>>>() {});
                        model.addAttribute("rememberedUsers", rememberedUsers);
                    } catch (Exception e) {
                        logger.error("Could not deserialize remembered-users cookie", e);
                    }
                    break;
                }
            }
        }
        
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model, HttpServletRequest request) {
        // S'assurer que la session est créée immédiatement pour éviter les problèmes CSRF
        HttpSession session = request.getSession(true);
        
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto user,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes,
                             HttpServletRequest request) {
        try {
            logger.info("=== DÉBUT INSCRIPTION (SANS MULTIPART) ===");
            logger.info("Username: {}", user.getUsername());
            logger.info("Email: {}", user.getEmail());
            logger.info("UserType: {}", user.getUserType());
            
            // Créer une session explicitement pour éviter les problèmes CSRF
            request.getSession(true);
            
            validateUserFields(user, result);

            if (result.hasErrors()) {
                logger.error("Erreurs de validation: {}", result.getAllErrors());
                return "auth/register";
            }

            // Utiliser l'image par défaut pour l'inscription
            String defaultImage = getDefaultImageForUserType(user.getUserType());
            user.setProfileImagePath(defaultImage);
            logger.info("Image par défaut utilisée: {}", defaultImage);

            logger.info("Appel du service d'inscription...");
            userService.registerNewUser(user);
            logger.info("=== INSCRIPTION RÉUSSIE ===");
            
            // Rediriger vers la page de configuration de photo de profil
            redirectAttributes.addFlashAttribute("username", user.getUsername());
            redirectAttributes.addFlashAttribute("userType", user.getUserType());
            return "redirect:/auth/profile-photo-setup";
            
        } catch (Exception e) {
            logger.error("=== ERREUR INSCRIPTION ===", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de l'inscription. Veuillez réessayer.");
            return "auth/register";
        }
    }

    @GetMapping("/register-simple")
    public String showSimpleRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        model.addAttribute("simpleMode", true);
        return "auth/register";
    }

    @PostMapping("/register-simple")
    public String registerUserSimple(@ModelAttribute("user") UserRegistrationDto user,
                                   BindingResult result,
                                   Model model,
                                   RedirectAttributes redirectAttributes) {
        try {
            logger.info("=== DÉBUT INSCRIPTION SIMPLE (SANS MULTIPART) ===");
            logger.info("Username: {}", user.getUsername());
            logger.info("Email: {}", user.getEmail());
            logger.info("UserType: {}", user.getUserType());
            
            validateUserFields(user, result);

            if (result.hasErrors()) {
                logger.error("Erreurs de validation: {}", result.getAllErrors());
                model.addAttribute("simpleMode", true);
                return "auth/register";
            }

            // Utiliser l'image par défaut
            String defaultImage = getDefaultImageForUserType(user.getUserType());
            user.setProfileImagePath(defaultImage);
            logger.info("Image par défaut utilisée: {}", defaultImage);

            logger.info("Appel du service d'inscription...");
            userService.registerNewUser(user);
            logger.info("=== INSCRIPTION SIMPLE RÉUSSIE ===");
            
            redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            logger.error("=== ERREUR INSCRIPTION SIMPLE ===", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de l'inscription. Veuillez réessayer.");
            model.addAttribute("simpleMode", true);
            return "auth/register";
        }
    }

    @GetMapping("/profile-photo-setup")
    public String showProfilePhotoSetup(Model model, RedirectAttributes redirectAttributes) {
        // Vérifier si les informations utilisateur sont disponibles
        if (!model.containsAttribute("username") || !model.containsAttribute("userType")) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expirée. Veuillez vous inscrire à nouveau.");
            return "redirect:/auth/register";
        }
        
        return "auth/profile-photo-setup";
    }

    @PostMapping("/profile-photo-setup")
    public String handleProfilePhotoSetup(@RequestParam("username") String username,
                                        @RequestParam("userType") String userType,
                                        @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                                        @RequestParam("action") String action,
                                        RedirectAttributes redirectAttributes) {
        try {
            logger.info("=== CONFIGURATION PHOTO DE PROFIL ===");
            logger.info("Username: {}", username);
            logger.info("UserType: {}", userType);
            logger.info("Action: {}", action);
            
            if ("upload".equals(action) && profileImage != null && !profileImage.isEmpty()) {
                // Uploader la nouvelle photo de profil
                String imagePath = handleProfileImageUpload(profileImage);
                userService.updateUserProfileImage(username, imagePath);
                logger.info("Photo de profil mise à jour: {}", imagePath);
                redirectAttributes.addFlashAttribute("successMessage", "Photo de profil ajoutée avec succès !");
            } else {
                logger.info("Utilisateur a choisi d'ignorer l'ajout de photo de profil");
                redirectAttributes.addFlashAttribute("successMessage", "Inscription terminée ! Vous pouvez maintenant vous connecter.");
            }
            
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            logger.error("Erreur lors de la configuration de la photo de profil", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Erreur lors de l'ajout de la photo. Vous pouvez vous connecter et l'ajouter plus tard.");
            return "redirect:/auth/login";
        }
    }
    
    
    @GetMapping("/home")
    public String home() {
        return "home";
    }
    
    @PostMapping("/login-remembered")
    public String loginRemembered(@RequestParam String username, HttpServletRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Authentication authentication = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());

        String role = userDetails.getAuthorities().iterator().next().getAuthority();
        switch (role) {
            case "ROLE_LIBRARIAN": return "redirect:/librarian/dashboard";
            case "ROLE_STAFF": return "redirect:/staff/dashboard";
            case "ROLE_STUDENT": return "redirect:/student/dashboard";
            default: return "redirect:/dashboard";
        }
    }

    @PostMapping("/remove-remembered-user")
    public String removeRememberedUser(@RequestParam String username, HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("remembered-users".equals(cookie.getName())) {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String decodedValue = new String(Base64.getUrlDecoder().decode(cookie.getValue()));
                        List<Map<String, String>> rememberedUsers = mapper.readValue(decodedValue, new TypeReference<List<Map<String, String>>>() {});
                        
                        rememberedUsers.removeIf(user -> user.get("username").equals(username));
                        
                        String updatedValue = Base64.getUrlEncoder().encodeToString(mapper.writeValueAsString(rememberedUsers).getBytes());
                        Cookie newCookie = new Cookie("remembered-users", updatedValue);
                        newCookie.setPath("/");
                        newCookie.setMaxAge(30 * 24 * 60 * 60); // 30 days
                        response.addCookie(newCookie);
                    } catch (Exception e) {
                        logger.error("Could not update remembered-users cookie", e);
                    }
                    break;
                }
            }
        }
        return "redirect:/auth/login";
    }

    private void validateUserFields(UserRegistrationDto user, BindingResult result) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            result.rejectValue("username", "error.user", "Le nom d'utilisateur est obligatoire");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            result.rejectValue("password", "error.user", "Le mot de passe est obligatoire");
        }
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            result.rejectValue("name", "error.user", "Le nom est obligatoire");
        }
        if (user.getPassword() != null && user.getConfirmPassword() != null) {
            if (!user.getPassword().equals(user.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.user", "Les mots de passe ne correspondent pas");
            }
        }
        
        // Validation spécifique selon le type d'utilisateur
        if (user.getUserType() != null) {
            switch (user.getUserType().toUpperCase()) {
                case "STUDENT":
                    // L'ID étudiant est maintenant généré automatiquement
                    // Pas de validation nécessaire pour studentId
                    break;
                case "STAFF":
                    // L'ID employé est maintenant généré automatiquement
                    // Validation seulement pour le département (optionnel)
                    break;
                case "LIBRARIAN":
                    // L'ID employé est maintenant généré automatiquement
                    // Pas de validation nécessaire pour employeeId
                    break;
            }
        } else {
            result.rejectValue("userType", "error.user", "Le type d'utilisateur est obligatoire");
        }
    }

    private String handleProfileImageUpload(MultipartFile profileImage) throws IOException {
        String contentType = profileImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }
        
        Path uploadDir = Paths.get(uploadPath, "profiles");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalFilename = profileImage.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        Path filePath = uploadDir.resolve(filename);
        Files.copy(profileImage.getInputStream(), filePath);

        return "/uploads/profiles/" + filename;
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
package com.library.controller;

import com.library.dto.UserRegistrationDto;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto user,
                             @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validation des champs obligatoires
            validateUserFields(user, result);

            if (result.hasErrors()) {
                return "auth/register";
            }

            // Traitement de l'image de profil
            if (profileImage != null && !profileImage.isEmpty()) {
                String profileImagePath = handleProfileImageUpload(profileImage);
                user.setProfileImagePath(profileImagePath);
            }

            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de l'inscription: " + e.getMessage());
            return "auth/register";
        }
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
        if (!user.getPassword().equals(user.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Les mots de passe ne correspondent pas");
        }
    }

    private String handleProfileImageUpload(MultipartFile profileImage) throws IOException {
        // Vérifier le type de fichier
        String contentType = profileImage.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Le fichier doit être une image");
        }

        // Créer le dossier d'upload s'il n'existe pas
        Path uploadDir = Paths.get(uploadPath, "profiles");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // Générer un nom de fichier unique
        String originalFilename = profileImage.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = UUID.randomUUID().toString() + extension;

        // Sauvegarder le fichier
        Path filePath = uploadDir.resolve(filename);
        Files.copy(profileImage.getInputStream(), filePath);

        // Retourner le chemin relatif pour accéder à l'image
        return "/uploads/profiles/" + filename;
    }
}
package com.library.controller;

import com.library.dto.UserRegistrationDto;
import com.library.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }
    @Autowired
    private UserService userService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new UserRegistrationDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") UserRegistrationDto user,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            // Validation des champs obligatoires
            if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
                result.rejectValue("username", "error.user", "Le nom d'utilisateur est obligatoire");
            }
            if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                result.rejectValue("password", "error.user", "Le mot de passe est obligatoire");
            }
            if (user.getName() == null || user.getName().trim().isEmpty()) {
                result.rejectValue("name", "error.user", "Le nom est obligatoire");
            }

            // Vérification des mots de passe
            if (!user.getPassword().equals(user.getConfirmPassword())) {
                result.rejectValue("confirmPassword", "error.user", "Les mots de passe ne correspondent pas");
            }

            if (result.hasErrors()) {
                return "auth/register";
            }

            userService.registerNewUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/auth/login";
            
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de l'inscription: " + e.getMessage());
            return "auth/register";
        }
    }
}
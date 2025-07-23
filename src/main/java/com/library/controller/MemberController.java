package com.library.controller;

import com.library.model.Member;
import com.library.model.Student;
import com.library.model.User;
import com.library.model.Librarian;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.library.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.library.enums.UserType;


@Controller
@RequestMapping("/librarian/members")
public class MemberController {

    @Autowired
    private UserService userService;
private static final Logger logger = LoggerFactory.getLogger(MemberController.class);
    @GetMapping
    public String manageMembers(Model model, java.security.Principal principal) {
        try {
            logger.info("Tentative de récupération de la liste des membres pour gestion");
            List<User> allUsers = userService.getAllMembers();
            logger.info("Nombre total d'utilisateurs récupérés : " + allUsers.size());
            
            // Grouper les utilisateurs par type
            Map<UserType, List<User>> usersByType = allUsers.stream()
                .collect(Collectors.groupingBy(User::getUserType));
            
            // Ajouter chaque groupe au modèle
            List<User> librarians = usersByType.getOrDefault(UserType.LIBRARIAN, List.of());
            List<User> students = usersByType.getOrDefault(UserType.STUDENT, List.of());
            List<User> staff = usersByType.getOrDefault(UserType.STAFF, List.of());
            
            model.addAttribute("librarians", librarians);
            model.addAttribute("students", students);
            model.addAttribute("staffMembers", staff); // Changé pour correspondre au template
            
            // Ajouter les statistiques
            model.addAttribute("totalLibrarians", librarians.size());
            model.addAttribute("totalStudents", students.size());
            model.addAttribute("totalStaff", staff.size());
            model.addAttribute("totalMembers", allUsers.size());
            
            // Ajouter l'utilisateur connecté
            if (principal != null) {
                try {
                    User currentUser = userService.getUserByUsername(principal.getName());
                    model.addAttribute("user", currentUser);
                } catch (Exception e) {
                    logger.warn("Impossible de récupérer l'utilisateur connecté", e);
                    // Créer un utilisateur par défaut pour éviter les erreurs
                    Librarian defaultUser = new Librarian();
                    defaultUser.setName("Utilisateur");
                    defaultUser.setEmail("user@example.com");
                    model.addAttribute("user", defaultUser);
                }
            }
            
            // Ajouter des variables par défaut pour éviter les erreurs
            model.addAttribute("unreadNotifications", 0);
            model.addAttribute("newBooksCount", 0);
            model.addAttribute("newMembersCount", 0);
            model.addAttribute("pendingBorrowsCount", 0);
            
            logger.info("Répartition des membres - Librarians: " + librarians.size() +
                       ", Students: " + students.size() + ", Staff: " + staff.size());
            
            return "librarian/members/manage";
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des membres", e);
            model.addAttribute("errorMessage", "Une erreur s'est produite lors de la récupération des membres");
            return "error";
        }
    }


     @GetMapping("/edit/{id}")
    public String showEditMemberForm(@PathVariable Long id, Model model) {
        try {
            logger.info("Récupération du membre pour édition, id=" + id);
            User user = userService.getUserById(id);
            model.addAttribute("member", user);
            return "librarian/members/form";
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération du membre", e);
            model.addAttribute("error", "Membre non trouvé");
            return "redirect:/librarian/members";
        }
    }

    @PostMapping("/edit/{id}")
    public String updateMember(@PathVariable Long id, 
                             @RequestParam("name") String name,
                             @RequestParam("email") String email,
                             @RequestParam("active") boolean active,
                             RedirectAttributes redirectAttributes) {
        try {
            logger.info("Tentative de mise à jour du membre id=" + id);
            logger.info("Données reçues : name=" + name + ", email=" + email + ", active=" + active);

            userService.updateUserFields(id, name, email, active);
            
            redirectAttributes.addFlashAttribute("success", "Membre mis à jour avec succès");
            return "redirect:/librarian/members";
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du membre", e);
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
            return "redirect:/librarian/members/edit/" + id;
        }
    }

   @PostMapping("/delete/{id}")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Membre supprimé avec succès");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression du membre");
        }
        return "redirect:/librarian/members"; // Changé pour correspondre au nouveau chemin
    }
}
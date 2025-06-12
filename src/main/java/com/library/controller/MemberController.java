package com.library.controller;

import com.library.model.Member;
import com.library.model.Student;
import com.library.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.library.service.UserService;

import java.time.LocalDateTime;
import java.util.List;

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
    public String listMembers(Model model) {
        try {
            logger.info("Tentative de récupération de la liste des membres");
            List<User> members = userService.getAllMembers();
            logger.info("Nombre de membres récupérés : " + members.size());
            model.addAttribute("members", members);
            return "librarian/members/list";
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
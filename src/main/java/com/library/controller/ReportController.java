package com.library.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties.Tomcat.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.library.service.UserService;
import com.library.service.BorrowingService;

import java.util.HashMap;
import java.util.Map;
@Controller
@RequestMapping("/librarian/reports")
public class ReportController {

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowingService borrowingService;

    @GetMapping("/members")
    public String getMemberReport(Model model) {
        Map<String, Object> statistics = new HashMap<>();
        
        // Nombre total de membres
        statistics.put("totalMembers", userService.getTotalMembers());
        
        // Membres actifs/inactifs
        statistics.put("activeMembers", userService.getActiveMembersCount());
        statistics.put("inactiveMembers", userService.getInactiveMembersCount());
        
        // Membres récents (inscrits dans les 30 derniers jours)
        statistics.put("newMembers", userService.getNewMembersCount());
        
        // Statistiques d'emprunts
        statistics.put("borrowingStats", borrowingService.getMemberBorrowingStats());
        
        model.addAttribute("statistics", statistics);
        return "librarian/reports/members";
    }

    // Possibilité d'ajouter d'autres types de rapports
    @GetMapping("/export")
    public ResponseEntity<Resource> exportMemberReport() {
        return null;
        // Code pour exporter le rapport en PDF ou Excel
    }
}
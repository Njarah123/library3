/*package com.library.controller;

import com.library.model.Book;
import com.library.service.BookService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.library.service.BookService;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

 @Controller
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private BookService bookService;

    @GetMapping
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Date et heure actuelles
        String currentDateTime = LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // Livres recommandés (limités à 8)
        Page<Book> bookPage = bookService.getAllBooks(PageRequest.of(0, 8));
        
        // Catégories avec leur nombre de livres
        List<String> categories = bookService.getAllCategories();
        Map<String, Long> categoryCount = new HashMap<>();
        
        // Compter le nombre de livres par catégorie
        for (String category : categories) {
            long count = bookService.countBooksByCategory(category);
            categoryCount.put(category, count);
        }

        // Ajouter les attributs au modèle
        model.addAttribute("currentDateTime", currentDateTime);
        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("recommendedBooks", bookPage.getContent());
        model.addAttribute("categories", categories);
        model.addAttribute("categoryCount", categoryCount);

        return "home";
    }
    }*/

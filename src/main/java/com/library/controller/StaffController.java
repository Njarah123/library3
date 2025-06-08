package com.library.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.service.BookService;
import com.library.service.BorrowingService;
@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowingService borrowingService;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        return "staff/dashboard";
    }
@GetMapping("/books")
    public String showBooks(Model model) {
        List<Book> allBooks = bookService.getAllBooks(); // Cette méthode doit retourner TOUS les livres
        model.addAttribute("books", allBooks);
        return "staff/books";
    }

    @GetMapping("/borrows")
    public String showBorrows(Model model) {
        return "staff/borrows";
    }

  @PostMapping("/rate")
    public String rateBook(@RequestParam Long borrowingId, 
                          @RequestParam Integer rating,
                          @RequestParam(required = false) String comment,
                          Principal principal,
                          RedirectAttributes redirectAttributes) {
        try {
            borrowingService.rateStaffBorrowing(borrowingId, principal.getName(), rating, comment);
            redirectAttributes.addFlashAttribute("success", "Note ajoutée avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la notation : " + e.getMessage());
        }
        return "redirect:/staff/history";
    }

@GetMapping("/history")
    public String showBorrowingHistory(Model model, Principal principal) {
        List<Borrowing> borrowingHistory = borrowingService.getUserBorrowingHistory(principal.getName());
        model.addAttribute("borrowings", borrowingHistory);
        return "staff/history";
    }
    
      @PostMapping("/books/borrow")
    public String borrowBook(@RequestParam Long bookId, Principal principal, RedirectAttributes redirectAttributes) {
        try {
            borrowingService.borrowBook(bookId, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Livre emprunté avec succès !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'emprunt : " + e.getMessage());
        }
        return "redirect:/staff/books";
    }
}
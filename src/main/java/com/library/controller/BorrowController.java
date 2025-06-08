package com.library.controller;

import com.library.model.Book;
import com.library.model.Borrow;
import com.library.model.User;
import com.library.security.CustomUserDetails;
import com.library.service.BookService;
import com.library.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class BorrowController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowService borrowService;

    @PostMapping("/borrow/{bookId}")
    public String borrowBook(@PathVariable Long bookId, 
                           @AuthenticationPrincipal CustomUserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        try {
            if (userDetails == null) {
                throw new RuntimeException("Utilisateur non connecté");
            }

            Book book = bookService.getBookById(bookId);
            if (book == null) {
                throw new RuntimeException("Livre non trouvé");
            }

            // Vérifier si le livre est disponible
            if (book.getAvailableQuantity() <= 0) {
                throw new RuntimeException("Ce livre n'est pas disponible pour l'emprunt");
            }

            // Vérifier si l'utilisateur a déjà emprunté ce livre
            if (borrowService.hasUserBorrowedBook(userDetails.getUser().getId(), bookId)) {
                throw new RuntimeException("Vous avez déjà emprunté ce livre");
            }

            // Créer l'emprunt
            Borrow borrow = new Borrow();
            borrow.setBook(book);
            borrow.setUser(userDetails.getUser());
            borrow.setBorrowDate(LocalDateTime.now());
            // Date de retour prévue (14 jours)
            borrow.setExpectedReturnDate(LocalDateTime.now().plusDays(14));
            borrow.setStatus("BORROWED");

            // Mettre à jour la quantité disponible du livre
            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
            bookService.updateBook(book);

            // Sauvegarder l'emprunt
            borrowService.saveBorrow(borrow);

            redirectAttributes.addFlashAttribute("success", "Livre emprunté avec succès!");

            // Rediriger selon le rôle
            String role = userDetails.getUser().getRole();
            if (role.equals("ROLE_STAFF")) {
                return "redirect:/staff/books";
            } else if (role.equals("ROLE_STUDENT")) {
                return "redirect:/student/books";
            } else {
                return "redirect:/books";
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'emprunt: " + e.getMessage());
            return "redirect:/books";
        }
    }
}
package com.library.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRepository;

/**
 * Controller for administrative book operations
 */
@Controller
@RequestMapping("/admin/books")
@PreAuthorize("hasRole('LIBRARIAN')")
public class BookAdminController {

    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowingRepository borrowingRepository;
    
    /**
     * Updates the totalBorrows field for all books based on their borrowing history.
     * This endpoint can be used to fix books that have incorrect totalBorrows values.
     * 
     * @param redirectAttributes For adding flash messages
     * @return Redirect to the book list page
     */
    @GetMapping("/update-total-borrows")
    public String updateAllBooksTotalBorrows(RedirectAttributes redirectAttributes) {
        List<Book> allBooks = bookRepository.findAll();
        int updatedCount = 0;
        
        for (Book book : allBooks) {
            // Count all borrowings for this book
            List<Borrowing> bookBorrowings = borrowingRepository.findByBookOrderByBorrowDateDesc(book);
            int borrowCount = bookBorrowings.size();
            
            // Only update if the count is different from the current value
            if (book.getTotalBorrows() == null || book.getTotalBorrows() != borrowCount) {
                book.setTotalBorrows(borrowCount);
                bookRepository.save(book);
                updatedCount++;
            }
        }
        
        redirectAttributes.addFlashAttribute("success", 
            "Mise à jour réussie! " + updatedCount + " livres ont été mis à jour avec leur nombre total d'emprunts.");
        
        return "redirect:/librarian/books";
    }
}
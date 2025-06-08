package com.library.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.library.service.FileStorageService;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.security.CustomUserDetails;
import com.library.service.BookService;
import com.library.service.BorrowingService;

import org.springframework.core.io.Resource;

@Controller
@RequestMapping("/librarian")
@PreAuthorize("hasRole('LIBRARIAN')")

public class LibrarianController {

    @Autowired
    private BookService bookService;
    private FileStorageService fileStorageService;
      private final BorrowingService borrowingService;
    
    @Autowired
    public LibrarianController(BorrowingService borrowingService) {
        this.borrowingService = borrowingService;
    }


     @PostMapping("/borrowings/{id}/return")
    @ResponseBody
    public ResponseEntity<?> returnBook(@PathVariable Long id) {
        try {
            borrowingService.returnBook(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erreur lors du retour du livre");
        }
    }


   @GetMapping("/borrows-books")
    public String showBorrowedBooks(Model model) {
        try {
            List<Borrowing> allBorrowings = borrowingService.getAllBorrowings();
            
            // Comptage des différents types d'emprunts
            long currentBorrowings = allBorrowings.stream()
                .filter(b -> "EMPRUNTE".equals(b.getStatus()))
                .count();
            
            long overdueBorrowings = allBorrowings.stream()
                .filter(b -> "EMPRUNTE".equals(b.getStatus()) && b.isOverdue())
                .count();
            
            long todayReturns = allBorrowings.stream()
                .filter(b -> "RETOURNE".equals(b.getStatus()) && 
                           b.getReturnDate() != null && 
                           b.getReturnDate().toLocalDate().equals(LocalDate.now()))
                .count();

            // Ajout des attributs au modèle
            model.addAttribute("borrowings", allBorrowings);
            model.addAttribute("totalBorrowings", allBorrowings.size());
            model.addAttribute("currentBorrowings", currentBorrowings);
            model.addAttribute("overdueBorrowings", overdueBorrowings);
            model.addAttribute("todayReturns", todayReturns);

            return "librarian/borrows-books";
        } catch (Exception e) {
            e.printStackTrace(); // Pour le débogage
            model.addAttribute("error", "Une erreur est survenue: " + e.getMessage());
            return "librarian/borrows-books";
        }
    }

    



    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalBooks", bookService.getTotalBooks());
        model.addAttribute("borrowedBooks", bookService.getBorrowedBooksCount());
        model.addAttribute("overdueBooks", bookService.getOverdueBooks());
        return "librarian/dashboard";
    }

    @GetMapping("/books")
public String listBooks(@RequestParam(required = false) String search, Model model) {
    List<Book> books;
    if (search != null && !search.isEmpty()) {
        // Recherche dans tous les livres (disponibles et non disponibles)
        books = bookService.searchAllBooks(search);
    } else {
        // Afficher tous les livres
        books = bookService.getAllBooks();
    }
    model.addAttribute("books", books);
    return "librarian/books";
}

@GetMapping("/books/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        Resource file = fileStorageService.loadFileAsResource(filename);
        
        return ResponseEntity.ok()
            .contentType(MediaType.IMAGE_JPEG) // ou MediaType.IMAGE_PNG selon le type d'image
            .body(file);
    }

    

    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "librarian/add-book";
    }

    @PostMapping("/books/add")
    public String addBook(@ModelAttribute Book book, 
                         @AuthenticationPrincipal CustomUserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            // Gestion du chemin de l'image
            if (book.getImagePath() != null && !book.getImagePath().isEmpty()) {
                if (!book.getImagePath().startsWith("/images/")) {
                    book.setImagePath("/images/" + book.getImagePath());
                }
            } else {
                book.setImagePath("/images/default-book.png");
            }

            // Définir les dates
            LocalDateTime now = LocalDateTime.now();
            book.setAddedDate(now);
            book.setLastUpdated(now);

            // Définir le librarian
            if (userDetails != null && userDetails.getUser() != null) {
                book.setLibrarian(userDetails.getUser());
            }

            // Valeurs par défaut
            book.setAvailable(true);
            book.setAvailableQuantity(book.getQuantity());
            book.setNumberOfRatings(0);
            book.setRating(0.0);
            book.setTotalBorrows(0);

            bookService.addBook(book);
            redirectAttributes.addFlashAttribute("success", "Livre ajouté avec succès!");
            return "redirect:/librarian/books";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur: " + e.getMessage());
            return "redirect:/librarian/books/add";
        }
    }


     @GetMapping("/books/edit/{id}")
    public String showEditBookForm(@PathVariable Long id, Model model) {
        Book book = bookService.getBookById(id);
        model.addAttribute("book", book);
        return "librarian/edit-book";
    }

   @PostMapping("/books/edit/{id}")
    public String updateBook(@PathVariable Long id, 
                           @ModelAttribute("book") Book book, 
                           RedirectAttributes redirectAttributes) {
        try {
            Book existingBook = bookService.getBookById(id);
            if (existingBook == null) {
                throw new RuntimeException("Livre non trouvé");
            }
            
            // Mettre à jour uniquement les champs modifiables
            existingBook.setTitle(book.getTitle());
            existingBook.setAuthor(book.getAuthor());
            existingBook.setIsbn(book.getIsbn());
            existingBook.setCategory(book.getCategory());
            existingBook.setPublisher(book.getPublisher());
            existingBook.setPublicationYear(book.getPublicationYear());
            existingBook.setLanguage(book.getLanguage());
            existingBook.setEdition(book.getEdition());
            existingBook.setQuantity(book.getQuantity());
            existingBook.setShelfLocation(book.getShelfLocation());
            existingBook.setDescription(book.getDescription());
            existingBook.setPurchasePrice(book.getPurchasePrice());
            existingBook.setReplacementCost(book.getReplacementCost());
            existingBook.setLastUpdated(LocalDateTime.now());
            
            // Gestion de l'image
            if (book.getImagePath() != null && !book.getImagePath().trim().isEmpty()) {
                if (!book.getImagePath().startsWith("/images/")) {
                    existingBook.setImagePath("/images/" + book.getImagePath());
                } else {
                    existingBook.setImagePath(book.getImagePath());
                }
            }
            
            bookService.updateBook(existingBook);
            redirectAttributes.addFlashAttribute("success", "Livre modifié avec succès!");
            return "redirect:/librarian/books";
            
        } catch (Exception e) {
            e.printStackTrace(); // Pour le debugging
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la modification: " + e.getMessage());
            return "redirect:/librarian/books";
        }
    }

    

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Book book = bookService.getBookById(id);
            if (book == null) {
                throw new RuntimeException("Livre non trouvé");
            }

            
            
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("success", "Livre supprimé avec succès!");
            return "redirect:/librarian/books";
            
        } catch (Exception e) {
            e.printStackTrace(); // Pour le debugging
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression: " + e.getMessage());
            return "redirect:/librarian/books";
        }
    }
}
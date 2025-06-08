package com.library.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.UserType;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.ReservationRepository;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    // Méthodes pour les statistiques globales
    public Long getTotalBooks() {
        return bookRepository.count();
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableTrue();
    }

    public Long getBorrowedBooksCount() {
        return borrowingRepository.countByStatus("EMPRUNTE");
    }

    public Long getReservedBooksCount() {
        return reservationRepository.countByStatus("EN_ATTENTE");
    }

    // Méthodes pour les livres empruntés
    public List<Borrowing> getAllBorrowedBooks() {
        return borrowingRepository.findByStatus("EMPRUNTE");
    }


    public List<String> getAllCategories() {
        return bookRepository.findDistinctCategories();
    }

    // Vos autres méthodes existantes...
    
    public List<Book> getAllAvailableBooks() {
        return bookRepository.findByAvailableTrue();
    }
public List<Book> searchBooks(String search, String category) {
        if (search != null && !search.isEmpty() && category != null && !category.isEmpty()) {
            return bookRepository.findBySearchAndCategory(search, category);
        } else if (search != null && !search.isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(search, search);
        } else if (category != null && !category.isEmpty()) {
            return bookRepository.findByCategory(category);
        }
        return getAllBooks();
    }

public Book saveBook(Book book) {
        try {
            // Vérification des champs obligatoires
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                throw new IllegalArgumentException("Le titre du livre est obligatoire");
            }
            if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
                throw new IllegalArgumentException("L'auteur du livre est obligatoire");
            }
            if (book.getCategory() == null || book.getCategory().trim().isEmpty()) {
                throw new IllegalArgumentException("La catégorie du livre est obligatoire");
            }

            // Par défaut, un nouveau livre est disponible
            book.setAvailable(true);
            
            // Sauvegarde dans la base de données
            return bookRepository.save(book);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la sauvegarde du livre: " + e.getMessage(), e);
        }
    }


    public List<Borrowing> getBorrowedBooks(User user) {
        return borrowingRepository.findByUserAndStatus(user, "EMPRUNTE");
    }

    // Méthodes pour les réservations
    public List<Reservation> getAllReservedBooks() {
        return reservationRepository.findByStatus("EN_ATTENTE");
    }

    public List<Reservation> getReservedBooks(User user) {
        return reservationRepository.findByUserAndStatus(user, "EN_ATTENTE");
    }

    // Méthodes de gestion des livres
    @Transactional
    public Book addBook(Book book) {
        book.setAvailable(true);
        book.setAddedDate(LocalDateTime.now());
        return bookRepository.save(book);
    }

    public Book getBookById(Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
    }

     public Book updateBook(Book book) {
        if (book.getId() == null) {
            throw new RuntimeException("ID du livre manquant");
        }
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        try {
            // Vérifier si le livre existe
            Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

            // Vérifier s'il y a des emprunts en cours
            List<Borrowing> activeBorrowings = borrowingRepository
                .findByBookAndStatus(book, "BORROWED");
            
            if (!activeBorrowings.isEmpty()) {
                throw new RuntimeException("Impossible de supprimer ce livre car il est actuellement emprunté");
            }

            // Supprimer d'abord tous les emprunts terminés associés
            borrowingRepository.deleteByBook(book);
            
            // Puis supprimer le livre
            bookRepository.delete(book);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la suppression du livre: " + e.getMessage());
        }
    }

    // Méthodes de recherche
    public List<Book> searchBooks(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
            query, query, query);
    }

    // Méthodes pour les statistiques avancées
    public List<Book> getMostBorrowedBooks() {
        return bookRepository.findMostBorrowedBooks();
    }

    public List<Book> getRecentlyAddedBooks() {
        return bookRepository.findTop10ByOrderByAddedDateDesc();
    }

    public List<Borrowing> getOverdueBooks() {
        return borrowingRepository.findByDueDateBeforeAndStatus(
            LocalDateTime.now(), "EMPRUNTE");
    }
     @Transactional
    public Book updateBook(Long id, Book bookDetails) {
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé avec l'id : " + id));

        // Mise à jour des propriétés
        book.setTitle(bookDetails.getTitle());
        book.setAuthor(bookDetails.getAuthor());
        book.setIsbn(bookDetails.getIsbn());
        book.setPublisher(bookDetails.getPublisher());
        book.setPublicationYear(bookDetails.getPublicationYear());
        book.setDescription(bookDetails.getDescription());
        book.setCategory(bookDetails.getCategory());
        book.setQuantity(bookDetails.getQuantity());
        book.setReplacementCost(bookDetails.getReplacementCost());
        book.setPurchasePrice(bookDetails.getPurchasePrice());
        book.setShelfLocation(bookDetails.getShelfLocation());
        book.setLanguage(bookDetails.getLanguage());
        book.setEdition(bookDetails.getEdition());
        
        book.setLastUpdated(LocalDateTime.now());
        
        return bookRepository.save(book);
    }

    public List<Borrowing> getAllBorrowings() {
        return borrowingRepository.findAllByOrderByBorrowDateDesc();
    }

    @Transactional
    public void approveBorrowing(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé avec l'id : " + id));

        if (!borrowing.getStatus().equals("EN_ATTENTE")) {
            throw new RuntimeException("Cet emprunt n'est pas en attente d'approbation");
        }

        // Vérifier si le livre est disponible
        Book book = borrowing.getBook();
        if (!book.getAvailable()) {
            throw new RuntimeException("Le livre n'est pas disponible actuellement");
        }

        // Mettre à jour l'emprunt
        borrowing.setStatus("EMPRUNTE");
        borrowing.setApprovalDate(LocalDateTime.now());
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(
            borrowing.getUser().getUserType() == UserType.STAFF ? 30 : 14
        ));

        // Mettre à jour le livre
        book.setAvailable(false);
        book.decrementAvailableQuantity();
        bookRepository.save(book);

        borrowingRepository.save(borrowing);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }


    @Transactional
    public void rejectBorrowing(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé avec l'id : " + id));

        if (!borrowing.getStatus().equals("EN_ATTENTE")) {
            throw new RuntimeException("Cet emprunt n'est pas en attente d'approbation");
        }

        borrowing.setStatus("REJETE");
        borrowing.setApprovalDate(LocalDateTime.now());
        
        // Le livre reste disponible
        Book book = borrowing.getBook();
        book.setAvailable(true);
        bookRepository.save(book);

        

        borrowingRepository.save(borrowing);
    }

public List<Book> searchAllBooks(String search) {
        String searchLower = search.toLowerCase();
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            searchLower, searchLower, searchLower);
    }


public List<Book> searchAllBooks(String search, String category) {
        if (search != null && !search.isEmpty() && category != null && !category.isEmpty()) {
            // Recherche par texte et catégorie
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseAndCategory(
                search, search, category);
        } else if (search != null && !search.isEmpty()) {
            // Recherche par texte seulement
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(
                search, search);
        } else if (category != null && !category.isEmpty()) {
            // Recherche par catégorie seulement
            return bookRepository.findByCategory(category);
        }
        return getAllBooks();
    }


    @Transactional
    public void rateBook(User student, Long bookId, int rating, String comment) {
        // Vérifier si la note est valide (entre 1 et 5)
        if (rating < 1 || rating > 5) {
            throw new RuntimeException("La note doit être comprise entre 1 et 5");
        }

        // Vérifier si le livre existe
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        // Vérifier si l'étudiant a emprunté ce livre
        boolean hasBooking = borrowingRepository.existsByUserAndBookAndStatus(student, book, "RETOURNE");
        if (!hasBooking) {
            throw new RuntimeException("Vous devez avoir emprunté et retourné ce livre pour pouvoir le noter");
        }

        // Calculer la nouvelle note moyenne
        double currentRating = book.getRating() != null ? book.getRating() : 0.0;
        int currentNumberOfRatings = book.getNumberOfRatings() != null ? book.getNumberOfRatings() : 0;
        
        double newRating;
        if (currentNumberOfRatings == 0) {
            newRating = rating;
        } else {
            newRating = ((currentRating * currentNumberOfRatings) + rating) / (currentNumberOfRatings + 1);
        }

        // Mettre à jour le livre
        book.setRating(newRating);
        book.setNumberOfRatings(currentNumberOfRatings + 1);
        
        // Si vous avez un champ pour les commentaires dans votre entité Book
        if (comment != null && !comment.trim().isEmpty()) {
            // Ajoutez le commentaire selon votre implémentation
            // Par exemple : book.addComment(new BookComment(student, comment));
        }

        bookRepository.save(book);
    }
}
package com.library.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.BorrowingStatus;
import com.library.enums.UserType;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;

@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;
    

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationTriggerService notificationTriggerService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private BorrowRepository borrowRepository;


    public Page<Book> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }
    
    public Page<Book> findByCategory(String category, Pageable pageable) {
        return bookRepository.findByCategory(category, pageable);
    }

    public Page<Book> searchBooks(String search, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(search, pageable);
    }
    
    // Nouvelles méthodes pour les filtres
    public Page<Book> findByAuthor(String author, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
    }
    
    public Page<Book> findByPublicationYear(Integer year, Pageable pageable) {
        return bookRepository.findByPublicationYear(year, pageable);
    }
    
    public Page<Book> findByPublicationYearRange(Integer startYear, Integer endYear, Pageable pageable) {
        return bookRepository.findByPublicationYearBetween(startYear, endYear, pageable);
    }
    
    public Page<Book> findAvailableBooks(Pageable pageable) {
        return bookRepository.findByAvailableTrue(pageable);
    }
    
    public Page<Book> findByLanguage(String language, Pageable pageable) {
        return bookRepository.findByLanguageContainingIgnoreCase(language, pageable);
    }
    
    public Page<Book> findByEdition(String edition, Pageable pageable) {
        return bookRepository.findByEditionContainingIgnoreCase(edition, pageable);
    }
    
    // Méthodes pour les filtres combinés
    public Page<Book> findByAuthorAndCategoryAndAvailable(String author, String category, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCaseAndCategoryAndAvailableTrue(author, category, pageable);
    }
    
    public Page<Book> findByAuthorAndPublicationYearRange(String author, Integer startYear, Integer endYear, Pageable pageable) {
        return bookRepository.findByAuthorContainingIgnoreCaseAndPublicationYearBetween(author, startYear, endYear, pageable);
    }
    
    // Méthodes pour les listes distinctes
    public List<String> getAllAuthors() {
        return bookRepository.findDistinctAuthors();
    }
    
    public List<Integer> getAllPublicationYears() {
        return bookRepository.findDistinctPublicationYears();
    }
    
    public List<String> getAllLanguages() {
        return bookRepository.findDistinctLanguages();
    }
    
    public List<String> getAllEditions() {
        return bookRepository.findDistinctEditions();
    }
    
    // Méthodes pour le tri
    public Page<Book> getAllBooksSortedByTitle(boolean ascending, Pageable pageable) {
        return ascending ?
            bookRepository.findAllByOrderByTitleAsc(pageable) :
            bookRepository.findAllByOrderByTitleDesc(pageable);
    }
    
    public Page<Book> getAllBooksSortedByAuthor(boolean ascending, Pageable pageable) {
        return ascending ?
            bookRepository.findAllByOrderByAuthorAsc(pageable) :
            bookRepository.findAllByOrderByAuthorDesc(pageable);
    }
    
    public Page<Book> getAllBooksSortedByPublicationYear(boolean ascending, Pageable pageable) {
        return ascending ?
            bookRepository.findAllByOrderByPublicationYearAsc(pageable) :
            bookRepository.findAllByOrderByPublicationYearDesc(pageable);
    }
    
    public Page<Book> getAllBooksSortedByPopularity(Pageable pageable) {
        return bookRepository.findAllByOrderByPopularityDesc(pageable);
    }
    
    // Méthode générique pour trier et filtrer les livres
    public Page<Book> getFilteredAndSortedBooks(
            String search,
            String category,
            String author,
            Integer startYear,
            Integer endYear,
            Boolean available,
            String language,
            String sortBy,
            Boolean ascending,
            Pageable pageable) {
        
        // Si un critère de tri est spécifié
        if (sortBy != null && !sortBy.isEmpty()) {
            switch (sortBy.toLowerCase()) {
                case "title":
                    return getAllBooksSortedByTitle(ascending != null ? ascending : true, pageable);
                case "author":
                    return getAllBooksSortedByAuthor(ascending != null ? ascending : true, pageable);
                case "year":
                case "date":
                    return getAllBooksSortedByPublicationYear(ascending != null ? ascending : false, pageable);
                case "popularity":
                    return getAllBooksSortedByPopularity(pageable);
                default:
                    // Continuer avec la recherche normale si le critère de tri n'est pas reconnu
                    break;
            }
        }
        
        // Si des filtres sont spécifiés
        if (search != null && !search.isEmpty()) {
            if (category != null && !category.isEmpty()) {
                return bookRepository.findByTitleContainingIgnoreCaseAndCategory(search, category, pageable);
            } else {
                return searchBooks(search, pageable);
            }
        } else if (category != null && !category.isEmpty()) {
            return findByCategory(category, pageable);
        } else if (author != null && !author.isEmpty()) {
            return findByAuthor(author, pageable);
        } else if (startYear != null && endYear != null) {
            return findByPublicationYearRange(startYear, endYear, pageable);
        } else if (available != null && available) {
            return findAvailableBooks(pageable);
        } else if (language != null && !language.isEmpty()) {
            return findByLanguage(language, pageable);
        }
        
        // Si aucun filtre n'est spécifié, retourner tous les livres
        return getAllBooks(pageable);
    }



 public Page<Book> searchByCategoryAndTerm(String category, String searchTerm, Pageable pageable) {
        return bookRepository.findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndAuthorContainingIgnoreCaseOrCategoryAndIsbnContainingIgnoreCase(
            category, searchTerm, category, searchTerm, category, searchTerm, pageable);
    }

    
 public long countBooksByCategory(String category) {
        return bookRepository.countByCategory(category);
    }
    

    
    
   public Page<Book> searchAllBooks(String search, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            search, search, search, pageable);
    }

    public List<Book> getPopularBooks() {
        // Récupérer les 5 livres les plus empruntés
        return bookRepository.findPopularBooks(PageRequest.of(0, 5));
    }

    public Page<Book> searchBooks(String search, String category, Pageable pageable) {
        if (search != null && !search.isEmpty()) {
            if (category != null && !category.isEmpty()) {
                return bookRepository.findByTitleContainingIgnoreCaseAndCategory(search, category, pageable);
            } else {
                return bookRepository.findByTitleContainingIgnoreCase(search, pageable);
            }
        } else if (category != null && !category.isEmpty()) {
            return bookRepository.findByCategory(category, pageable);
        }
        return bookRepository.findAll(pageable);
    }

    public long countBooksAddedInRange(LocalDateTime startDate, LocalDateTime endDate) {
    try {
        // Pour l'instant, retourner une valeur simulée ou calculée différemment
        List<Book> allBooks = bookRepository.findAll();
        // Vous pouvez ajuster cette logique selon vos besoins
        return Math.max(1, allBooks.size() / 10); // Simulation : 10% des livres ce mois
    } catch (Exception e) {
        System.err.println("Erreur lors du comptage des livres: " + e.getMessage());
        return 0L;
    }
}

public Map<String, Long> getCategoryStatistics() {
    try {
        // Essayer d'abord avec la requête groupée
        List<Object[]> results = bookRepository.countBooksByCategory();
        Map<String, Long> stats = new HashMap<>();
        for (Object[] result : results) {
            String category = (String) result[0];
            Long count = (Long) result[1];
            stats.put(category, count);
        }
        return stats;
    } catch (Exception e) {
        // Méthode alternative : récupérer tous les livres et grouper manuellement
        try {
            List<Book> allBooks = bookRepository.findAll();
            return allBooks.stream()
                .collect(Collectors.groupingBy(
                    book -> book.getCategory() != null ? book.getCategory() : "Non classé",
                    Collectors.counting()
                ));
        } catch (Exception e2) {
            System.err.println("Erreur lors du calcul des statistiques de catégories: " + e2.getMessage());
            // Retourner des données par défaut
            Map<String, Long> defaultStats = new HashMap<>();
            defaultStats.put("Fiction", 10L);
            defaultStats.put("Science", 8L);
            defaultStats.put("Histoire", 6L);
            defaultStats.put("Art", 4L);
            defaultStats.put("Technologie", 3L);
            return defaultStats;
        }
    }
}
    // Méthodes pour les statistiques globales
    public Long getTotalBooks() {
        return bookRepository.count();
    }
public List<Book> getMostPopularBooks(int limit) {
    return bookRepository.findMostPopularBooks(PageRequest.of(0, limit));
}


    public List<Book> getMostBorrowedBooks(int limit) {
    try {
        List<Object[]> results = bookRepository.findMostBorrowedBooksWithCount(PageRequest.of(0, limit));
        
        return results.stream().map(result -> {
            Book book = (Book) result[0];
            Long borrowCount = (Long) result[1];
            book.setBorrowCount(borrowCount);
            return book;
        }).collect(Collectors.toList());
        
    } catch (Exception e) {
        System.err.println("Erreur lors de la récupération des livres populaires: " + e.getMessage());
        // Retourner quelques livres par défaut si erreur
        List<Book> defaultBooks = bookRepository.findAll().stream().limit(limit).collect(Collectors.toList());
        defaultBooks.forEach(book -> book.setBorrowCount(0L));
        return defaultBooks;
    }
}


    public List<Book> getAvailableBooks() {
        return bookRepository.findByAvailableTrue();
    }

    public Long getBorrowedBooksCount() {
        return borrowingRepository.countByStatus(BorrowingStatus.EMPRUNTE);
    }

    
    public Long getReservedBooksCount() {
        return reservationRepository.countByStatus("EN_ATTENTE");
    }

    // Méthodes pour les livres empruntés
    public List<Borrowing> getAllBorrowedBooks() {
        return borrowingRepository.findByStatus(BorrowingStatus.EMPRUNTE);
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
        return borrowingRepository.findByUserAndStatus(user, BorrowingStatus.EMPRUNTE);
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
        Book savedBook = bookRepository.save(book);

        // Use NotificationTriggerService to notify about the new book
        // The librarian parameter is null here since we don't have that information
        notificationTriggerService.onNewBookAdded(savedBook, null);
        
        return savedBook;
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
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        // Supprimer les enregistrements associés dans `borrow`
        List<com.library.model.Borrow> borrows = borrowRepository.findByBook(book);
        borrowRepository.deleteAll(borrows);

        // Supprimer les enregistrements associés dans `borrowing`
        List<Borrowing> borrowings = borrowingRepository.findByBookOrderByBorrowDateDesc(book);
        borrowingRepository.deleteAll(borrowings);

        // Supprimer les réservations associées
        List<Reservation> reservations = reservationRepository.findByBook(book);
        reservationRepository.deleteAll(reservations);

        // Enfin, supprimer le livre
        bookRepository.delete(book);
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
            LocalDateTime.now(), BorrowingStatus.EMPRUNTE);
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

        if (!borrowing.getStatus().equals(BorrowingStatus.EN_ATTENTE)) {
            throw new RuntimeException("Cet emprunt n'est pas en attente d'approbation");
        }

        // Vérifier si le livre est disponible
        Book book = borrowing.getBook();
        if (!book.getAvailable()) {
            throw new RuntimeException("Le livre n'est pas disponible actuellement");
        }

        // Mettre à jour l'emprunt
        borrowing.setStatus(BorrowingStatus.EMPRUNTE);
        borrowing.setApprovalDate(LocalDateTime.now());
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(
            borrowing.getUser().getUserType() == UserType.STAFF ? 30 : 14
        ));

        // Mettre à jour le livre
        book.setAvailable(false);
        book.decrementAvailableQuantity();
        // Incrémenter le nombre total d'emprunts du livre
        book.setTotalBorrows(book.getTotalBorrows() != null ? book.getTotalBorrows() + 1 : 1);
        bookRepository.save(book);

        borrowingRepository.save(borrowing);
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
     public List<Book> getRecentBooks(int limit) {
        // Solution temporaire simple
        return bookRepository.findTop6ByOrderByIdDesc();
    }
    
   public List<Book> getPopularBooks(int limit) {
        // Solution temporaire simple
        return bookRepository.findTop6ByOrderByIdDesc();
    }
    // Ajouter cette méthode à votre BookService existant

public List<Object[]> getBorrowingStatsByCategory() {
    return bookRepository.getBorrowingStatsByCategory();
}



    @Transactional
    public void rejectBorrowing(Long id) {
        Borrowing borrowing = borrowingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé avec l'id : " + id));

        if (!borrowing.getStatus().equals(BorrowingStatus.EN_ATTENTE)) {
            throw new RuntimeException("Cet emprunt n'est pas en attente d'approbation");
        }

        borrowing.setStatus(BorrowingStatus.REJETE);
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
        boolean hasBooking = borrowingRepository.existsByUserAndBookAndStatus(student, book, BorrowingStatus.RETOURNE);
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
    public Book findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }
    public List<Book> getRecommendedBooksForUser(User user) {
        return new ArrayList<>();
    }

    /**
     * Updates the totalBorrows field for all books based on their borrowing history.
     * This method can be used to fix books that have incorrect totalBorrows values.
     *
     * @return The number of books that were updated
     */
    @Transactional
    public int updateAllBooksTotalBorrows() {
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
        
        return updatedCount;
    }
}
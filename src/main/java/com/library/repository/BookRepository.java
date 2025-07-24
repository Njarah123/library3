package com.library.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.model.Book;

public interface BookRepository extends JpaRepository<Book, Long> {
    Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(
        String title, String author, String category, Pageable pageable);
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Book> findByCategory(String category, Pageable pageable);
    Page<Book> findByTitleContainingIgnoreCaseAndCategory(String title, String category, Pageable pageable);

    // Nouvelles méthodes pour les filtres
    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);
    Page<Book> findByPublicationYear(Integer year, Pageable pageable);
    Page<Book> findByPublicationYearBetween(Integer startYear, Integer endYear, Pageable pageable);
    Page<Book> findByAvailableTrue(Pageable pageable);
    Page<Book> findByLanguageContainingIgnoreCase(String language, Pageable pageable);
    Page<Book> findByEditionContainingIgnoreCase(String edition, Pageable pageable);
    
    // Méthodes combinées pour les filtres multiples
    Page<Book> findByAuthorContainingIgnoreCaseAndCategoryAndAvailableTrue(
        String author, String category, Pageable pageable);
    Page<Book> findByAuthorContainingIgnoreCaseAndPublicationYearBetween(
        String author, Integer startYear, Integer endYear, Pageable pageable);
    Page<Book> findByLanguageAndAvailableTrue(String language, Pageable pageable);
    
    List<Book> findByAvailableTrue();
    
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
        String title, String author, String isbn);
    
    // Correction de la requête problématique
    @Query("SELECT b FROM Book b ORDER BY SIZE(b.borrowings) DESC")
    List<Book> findMostBorrowedBooks();
    
    List<Book> findTop10ByOrderByAddedDateDesc();
    
    @Query("SELECT b FROM Book b WHERE b.quantity <= :threshold")
    List<Book> findLowStockBooks(int threshold);

     // Ajouter cette nouvelle méthode
     @Query("SELECT DISTINCT b.category FROM Book b ORDER BY b.category")
     List<String> findDistinctCategories();
     
     // Nouvelles méthodes pour les listes distinctes
     @Query("SELECT DISTINCT b.author FROM Book b ORDER BY b.author")
     List<String> findDistinctAuthors();
     
     @Query("SELECT DISTINCT b.publicationYear FROM Book b WHERE b.publicationYear IS NOT NULL ORDER BY b.publicationYear DESC")
     List<Integer> findDistinctPublicationYears();
     
     @Query("SELECT DISTINCT b.language FROM Book b WHERE b.language IS NOT NULL ORDER BY b.language")
     List<String> findDistinctLanguages();
     
     @Query("SELECT DISTINCT b.edition FROM Book b WHERE b.edition IS NOT NULL ORDER BY b.edition")
     List<String> findDistinctEditions();
      @Query("SELECT b FROM Book b WHERE (LOWER(b.title) LIKE LOWER('%' || :search || '%') OR LOWER(b.author) LIKE LOWER('%' || :search || '%')) AND b.category = :category")
    List<Book> findBySearchAndCategory(@Param("search") String search, @Param("category") String category);
    
    List<Book> findByTitleContainingIgnoreCaseAndCategoryAndAvailableTrue(String title, String category);
    
    List<Book> findByTitleContainingIgnoreCaseAndAvailableTrue(String title);
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(
        String title, String author, String category);
        List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseAndCategory(
        String title, String author, String category);
    
    List<Book> findByCategory(String category);
    
    // Méthodes de tri
    Page<Book> findAllByOrderByTitleAsc(Pageable pageable);
    Page<Book> findAllByOrderByTitleDesc(Pageable pageable);
    Page<Book> findAllByOrderByAuthorAsc(Pageable pageable);
    Page<Book> findAllByOrderByAuthorDesc(Pageable pageable);
    Page<Book> findAllByOrderByPublicationYearDesc(Pageable pageable);
    Page<Book> findAllByOrderByPublicationYearAsc(Pageable pageable);
    
    // Tri par popularité (basé sur le nombre d'emprunts)
    @Query("SELECT b FROM Book b LEFT JOIN b.borrowings br GROUP BY b ORDER BY COUNT(br) DESC")
    Page<Book> findAllByOrderByPopularityDesc(Pageable pageable);
     @Query("SELECT b FROM Book b LEFT JOIN b.borrowings br GROUP BY b ORDER BY COUNT(br) DESC")
    List<Book> findPopularBooks(Pageable pageable);


@Query("SELECT b.category, COUNT(b) FROM Book b WHERE b.category IS NOT NULL GROUP BY b.category ORDER BY COUNT(b) DESC")
List<Object[]> countBooksByCategory();

@Query("SELECT b, COUNT(br.id) as borrowCount FROM Book b " +
       "LEFT JOIN Borrowing br ON b.id = br.book.id " +
       "GROUP BY b.id ORDER BY COUNT(br.id) DESC")
List<Object[]> findMostBorrowedBooksWithCount(Pageable pageable);
 @Query("SELECT b FROM Book b ORDER BY b.id DESC")
    List<Book> findRecentBooks(Pageable pageable);
     List<Book> findTop6ByOrderByIdDesc();
@Query("SELECT b FROM Book b " +
       "LEFT JOIN Borrowing br ON br.book = b " +
       "GROUP BY b " +
       "ORDER BY COUNT(br) DESC, b.rating DESC")
List<Book> findMostPopularBooks(Pageable pageable);


@Query("SELECT COUNT(br) FROM Borrowing br WHERE br.book = :book")
Long countBorrowingsByBook(@Param("book") Book book);

@Query("SELECT b FROM Book b LEFT JOIN Borrowing br ON b.id = br.book.id " +
       "GROUP BY b.id ORDER BY COUNT(br.id) DESC")
List<Book> findMostBorrowedBooks(Pageable pageable);


     Page<Book> findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndAuthorContainingIgnoreCaseOrCategoryAndIsbnContainingIgnoreCase(
        String category1, String title,
        String category2, String author,
        String category3, String isbn,
        Pageable pageable);
long countByCategory(String category);
// Ajouter cette méthode

@Query("SELECT b.category, COUNT(br) FROM Book b " +
       "LEFT JOIN Borrowing br ON b = br.book " +
       "GROUP BY b.category " +
       "ORDER BY COUNT(br) DESC")
List<Object[]> getBorrowingStatsByCategory();
@Query("SELECT b FROM Book b WHERE b.category IN :categories AND b.id NOT IN :excludeIds AND b.available = true " +
       "ORDER BY (SELECT COUNT(br) FROM Borrowing br WHERE br.book = b) DESC")
List<Book> findRecommendedBooksByCategories(@Param("categories") List<String> categories, 
                                          @Param("excludeIds") List<Long> excludeIds, 
                                          Pageable pageable);

@Query("SELECT b FROM Book b WHERE b.available = true " +
       "ORDER BY (SELECT COUNT(br) FROM Borrowing br WHERE br.book = b) DESC")
List<Book> findTop10ByOrderByBorrowCountDesc(Pageable pageable);

// Méthode alternative si les excludeIds sont vides
@Query("SELECT b FROM Book b WHERE b.category IN :categories AND b.available = true " +
       "ORDER BY (SELECT COUNT(br) FROM Borrowing br WHERE br.book = b) DESC")
List<Book> findRecommendedBooksByCategoriesOnly(@Param("categories") List<String> categories, 
                                               Pageable pageable);
default List<Book> findTop10ByOrderByBorrowCountDesc() {
    return findTop10ByOrderByBorrowCountDesc(PageRequest.of(0, 10));
}


}

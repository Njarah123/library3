package com.library.repository;

import java.util.List;

import org.springframework.data.domain.Page;
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
      @Query("SELECT b FROM Book b WHERE (LOWER(b.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.author) LIKE LOWER(CONCAT('%', :search, '%'))) AND b.category = :category")
    List<Book> findBySearchAndCategory(@Param("search") String search, @Param("category") String category);
    
    List<Book> findByTitleContainingIgnoreCaseAndCategoryAndAvailableTrue(String title, String category);
    
    List<Book> findByTitleContainingIgnoreCaseAndAvailableTrue(String title);
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrCategoryContainingIgnoreCase(
        String title, String author, String category);
        List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
    
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseAndCategory(
        String title, String author, String category);
    
    List<Book> findByCategory(String category);
     @Query("SELECT b FROM Book b LEFT JOIN b.borrowings br GROUP BY b ORDER BY COUNT(br) DESC")
    List<Book> findPopularBooks(Pageable pageable);

     Page<Book> findByCategoryAndTitleContainingIgnoreCaseOrCategoryAndAuthorContainingIgnoreCaseOrCategoryAndIsbnContainingIgnoreCase(
        String category1, String title,
        String category2, String author,
        String category3, String isbn,
        Pageable pageable);
long countByCategory(String category);
}
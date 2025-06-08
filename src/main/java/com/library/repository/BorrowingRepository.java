package com.library.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.library.enums.BorrowingStatus;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.User;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

     int countByUserAndStatus(User user, String status);
    
    int countByUserAndStatusAndDueDateBefore(User user, String status, LocalDateTime date);
    
    int countByUser(User user);
    
    List<Borrowing> findByUserAndStatusAndDueDateBetween(
        User user, String status, LocalDateTime start, LocalDateTime end);
        List<Borrowing> findByBookAndStatus(Book book, String status);
    void deleteByBook(Book book);
    

  

    @Query("SELECT b FROM Borrowing b " +
           "JOIN FETCH b.book " +
           "JOIN FETCH b.user " +
           "ORDER BY b.borrowDate DESC")
    List<Borrowing> findAllWithBooksAndUsers();
    
    @Query("SELECT b FROM Borrowing b WHERE b.status IN :statuses")
    List<Borrowing> findByStatusIn(List<BorrowingStatus> statuses);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = :status")
    long countByStatus(BorrowingStatus status);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = :status AND b.dueDate < :date")
    long countByStatusAndDueDateBefore(BorrowingStatus status, LocalDateTime date);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = :status AND b.returnDate BETWEEN :start AND :end")
    long countByStatusAndReturnDateBetween(BorrowingStatus status, LocalDateTime start, LocalDateTime end);

    
    @Query("SELECT COUNT(DISTINCT b.book) FROM Borrowing b WHERE b.user = ?1")
    int countDistinctBookByUser(User user);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = ?1 AND b.returnDate > b.dueDate")
    int countByUserAndReturnDateAfterDueDate(User user);

    @Query("SELECT b FROM Borrowing b WHERE b.status = :status AND b.dueDate < CURRENT_TIMESTAMP")
    List<Borrowing> findOverdueBorrowings(@Param("status") BorrowingStatus status);
    
    int countByUserAndRatingIsNotNull(User user);
    
    public List<Borrowing> findByStatus(BorrowingStatus status);
    // Recherche par utilisateur
    List<Borrowing> findByUserOrderByBorrowDateDesc(User user);
    
    List<Borrowing> findByUserAndStatus(User user, String status);
    // Comptage des emprunts par statut
    Long countByStatus(String status);
    
    // Recherche des emprunts en retard
    List<Borrowing> findByDueDateBeforeAndStatus(LocalDateTime date, String status);
    
    // Vérification si un livre est emprunté
    boolean existsByBookAndStatus(Book book, String status);
    
    // Tous les emprunts triés par date
    List<Borrowing> findAllByOrderByBorrowDateDesc();
    
    // Emprunts actifs d'un utilisateur
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.status = 'EMPRUNTE'")
    int countActiveBooksByUser(@Param("user") User user);
    
    // Emprunts en retard d'un utilisateur
    List<Borrowing> findByUserAndStatusAndDueDateBefore(User user, String status, LocalDateTime date);
    
    // Emprunts par livre
    List<Borrowing> findByBookOrderByBorrowDateDesc(Book book);
    
    // Emprunts par statut
    List<Borrowing> findByStatus(String status);
     boolean existsByUserAndBookAndStatus(User user, Book book, String status);
    
    // Emprunts entre deux dates
    List<Borrowing> findByBorrowDateBetweenOrderByBorrowDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    // Emprunts en attente d'approbation
    List<Borrowing> findByStatusOrderByBorrowDateAsc(String status);
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, String status);
    
    // Historique des emprunts d'un utilisateur
    @Query("SELECT b FROM Borrowing b WHERE b.user = :user ORDER BY b.borrowDate DESC")
    List<Borrowing> findUserBorrowingHistory(@Param("user") User user);
    
    // Statistiques des emprunts par utilisateur
    @Query("SELECT b.user, COUNT(b) as borrowCount FROM Borrowing b GROUP BY b.user ORDER BY borrowCount DESC")
    List<Object[]> findBorrowingStatsByUser();
}
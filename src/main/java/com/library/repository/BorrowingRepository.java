package com.library.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;

import com.library.dto.MemberBorrowingStats;
import com.library.enums.BorrowingStatus;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.User;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long>, PagingAndSortingRepository<Borrowing, Long> {
    // Ajout de la méthode pour la pagination
    @Query("SELECT b FROM Borrowing b ORDER BY b.borrowDate DESC")
    Page<Borrowing> findAllBorrowings(Pageable pageable);
    // Nouvelles méthodes de pagination avec Pageable
    Page<Borrowing> findAll(Pageable pageable);
    
    Page<Borrowing> findByStatus(String status, Pageable pageable);
    
    Page<Borrowing> findByUser(User user, Pageable pageable);
     List<Borrowing> findByUserAndReturnDateIsNull(User user);
     
Long countByMemberId(Long memberId);
    Long countByMemberIdAndDueDateBefore(Long memberId, LocalDateTime date);
    Long countByMemberIdAndReturnDateIsNull(Long memberId);
    // Méthodes existantes
    int countByUserAndStatus(User user, String status);
     @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.member.id = :userId")
    Long countBorrowingsByUserId(@Param("userId") Long userId);
      @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.member.id = :userId AND b.dueDate < :date AND b.returnDate IS NULL")
    Long countLateBorrowingsByUserId(@Param("userId") Long userId, @Param("date") LocalDateTime date);

   @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.returnDate IS NULL")
    Long countCurrentBorrowingsByUserId(@Param("userId") Long userId);
@Query("SELECT b FROM Borrowing b WHERE b.fineAmount > 0")
    List<Borrowing> findAllWithFines();
      @Query("SELECT b FROM Borrowing b WHERE b.renewalCount > 0 AND b.user.id = :userId")
    List<Borrowing> findAllRenewedByUserId(@Param("userId") Long userId);
    int countByUserAndStatusAndDueDateBefore(User user, String status, LocalDateTime date);
    
    List<Borrowing> findByStatusAndDueDateBefore(String status, LocalDateTime date);
    int countByUser(User user);
    List<Borrowing> findTop10ByOrderByBorrowingDateDesc();
 
 @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate IS NULL")
    Long countByReturnDateIsNull();
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId")
    Long findBorrowingsCountByUserId(@Param("userId") Long userId);
     @Query("SELECT b FROM Borrowing b WHERE b.status = 'EN_ATTENTE'")
    List<Borrowing> findActiveBorrowings();
      @Query("SELECT b FROM Borrowing b WHERE b.status = 'EN_ATTENTE' AND b.dueDate < CURRENT_TIMESTAMP")
    List<Borrowing> findOverdueBorrowings();

     @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.status = :status")
    List<Borrowing> findBorrowingsByUserIdAndStatus(
        @Param("userId") Long userId, 
        @Param("status") String status
    );
     @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.dueDate < :date")
    List<Borrowing> findByUserIdAndDueDateBefore(
        @Param("userId") Long userId, 
        @Param("date") LocalDateTime date
    );
     @Query("SELECT b FROM Borrowing b WHERE b.returnDate IS NULL")
    List<Borrowing> findCurrentBorrowings();
    
    
    @Query("SELECT b FROM Borrowing b WHERE b.returnDate IS NULL AND b.dueDate < :currentDate")
    List<Borrowing> findByReturnDateIsNullAndDueDateBefore(@Param("currentDate") LocalDateTime currentDate);
    List<Borrowing> findByUserAndStatusAndDueDateBetween(
        User user, String status, LocalDateTime start, LocalDateTime end);
        
    List<Borrowing> findByBookAndStatus(Book book, String status);
    
    void deleteByBook(Book book);
     @Query("SELECT new com.library.dto.MemberBorrowingStats(" +
           "b.user.name, " +
           "COUNT(b), " +
           "SUM(CASE WHEN b.returnDate IS NULL AND b.dueDate < :currentDate THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN b.returnDate IS NULL THEN 1 ELSE 0 END)) " +
           "FROM Borrowing b " +
           "WHERE b.user = :user " +
           "GROUP BY b.user.name")
    MemberBorrowingStats getUserStats(@Param("user") User user, @Param("currentDate") LocalDateTime currentDate);
     @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate IS NULL")
    Long countActiveLoans();
  @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.returnDate IS NULL")
    List<Borrowing> findCurrentBorrowingsByUserId(@Param("userId") Long userId);
      @Query("SELECT b FROM Borrowing b WHERE b.status = 'EN_ATTENTE' AND b.dueDate < :now")
    List<Borrowing> findOverdueBorrowings(@Param("now") LocalDateTime now);
     @Query("SELECT b FROM Borrowing b WHERE b.dueDate < :date AND b.status = 'EN_ATTENTE'")
    List<Borrowing> findAllLateLoans(@Param("date") LocalDateTime date);
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.status = 'EN_ATTENTE'")
    Long countCurrentBorrowings(@Param("userId") Long userId);
     @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.status = 'EN_ATTENTE'")
    List<Borrowing> findCurrentBorrowings(@Param("userId") Long userId);
      @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.dueDate < :date AND b.returnDate IS NULL")
    List<Borrowing> findLateLoans(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.dueDate < :date AND b.returnDate IS NULL")
    Long countLateLoans(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    @Query("SELECT b FROM Borrowing b " +
           "JOIN FETCH b.book " +
           "JOIN FETCH b.user " +
           "ORDER BY b.borrowDate DESC")
    List<Borrowing> findAllWithBooksAndUsers();
    @Query("SELECT b FROM Borrowing b ORDER BY b.borrowDate DESC")
    Page<Borrowing> findAllByOrderByBorrowDateDesc(Pageable pageable);
    
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
    
    List<Borrowing> findByStatus(BorrowingStatus status);
    
    List<Borrowing> findByUserOrderByBorrowDateDesc(User user);
    
    List<Borrowing> findByUserAndStatus(User user, String status);
    
    Long countByStatus(String status);
    
    
    List<Borrowing> findByDueDateBeforeAndStatus(LocalDateTime date, String status);
    
    boolean existsByBookAndStatus(Book book, String status);
    
    List<Borrowing> findAllByOrderByBorrowDateDesc();
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.status = 'EMPRUNTE'")
    int countActiveBooksByUser(@Param("user") User user);
    
    List<Borrowing> findByUserAndStatusAndDueDateBefore(User user, String status, LocalDateTime date);
    
    List<Borrowing> findByBookOrderByBorrowDateDesc(Book book);
    
     @Query("SELECT b FROM Borrowing b WHERE b.status = :status")
    List<Borrowing> findByStatus(@Param("status") String status);
    
    boolean existsByUserAndBookAndStatus(User user, Book book, String status);
    
    List<Borrowing> findByBorrowDateBetweenOrderByBorrowDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Borrowing> findByStatusOrderByBorrowDateAsc(String status);
    
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, String status);
    
    @Query("SELECT b FROM Borrowing b WHERE b.user = :user ORDER BY b.borrowDate DESC")
    List<Borrowing> findUserBorrowingHistory(@Param("user") User user);
    
    @Query("SELECT b.user, COUNT(b) as borrowCount FROM Borrowing b GROUP BY b.user ORDER BY borrowCount DESC")
    List<Object[]> findBorrowingStatsByUser();
    int countByUserAndReturnDateIsNull(User user);
    List<Borrowing> findByUserAndReturnDateIsNullOrderByBorrowDateDesc(User user);
}
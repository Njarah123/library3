package com.library.repository;

import java.time.LocalDate;
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
    
    Page<Borrowing> findByStatus(BorrowingStatus status, Pageable pageable);
    
    Page<Borrowing> findByUser(User user, Pageable pageable);
    List<Borrowing> findByUserAndReturnDateIsNull(User user);
     
    // Fixed: Changed member.id to user.id
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId")
    Long countByMemberId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.dueDate < :date")
    Long countByMemberIdAndDueDateBefore(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.returnDate IS NULL")
    Long countByMemberIdAndReturnDateIsNull(@Param("userId") Long userId);
    
    // Méthodes existantes
    int countByUserAndStatus(User user, BorrowingStatus status);
    
    // Fixed: Changed member.id to user.id
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId")
    Long countBorrowingsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.dueDate < :date AND b.returnDate IS NULL")
    Long countLateBorrowingsByUserId(@Param("userId") Long userId, @Param("date") LocalDateTime date);

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.returnDate IS NULL")
    Long countCurrentBorrowingsByUserId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Borrowing b WHERE b.fineAmount > 0")
    List<Borrowing> findAllWithFines();
    
    @Query("SELECT b FROM Borrowing b WHERE b.renewalCount > 0 AND b.user.id = :userId")
    List<Borrowing> findAllRenewedByUserId(@Param("userId") Long userId);
    
    int countByUserAndStatusAndDueDateBefore(User user, BorrowingStatus status, LocalDateTime date);
    
    List<Borrowing> findByStatusAndDueDateBefore(BorrowingStatus status, LocalDateTime date);
    int countByUser(User user);
    
    // CORRIGÉ: Changé borrowingDate en borrowDate
    List<Borrowing> findTop10ByOrderByBorrowDateDesc();
 
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate IS NULL")
    Long countByReturnDateIsNull();
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId")
    Long findBorrowingsCountByUserId(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Borrowing b WHERE b.status = com.library.enums.BorrowingStatus.EN_ATTENTE")
    List<Borrowing> findActiveBorrowings();
    
    @Query("SELECT b FROM Borrowing b WHERE b.status = com.library.enums.BorrowingStatus.EN_ATTENTE AND b.dueDate < CURRENT_TIMESTAMP")
    List<Borrowing> findOverdueBorrowings();

    @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.status = :status")
    List<Borrowing> findBorrowingsByUserIdAndStatus(
        @Param("userId") Long userId,
        @Param("status") BorrowingStatus status
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
        User user, BorrowingStatus status, LocalDateTime start, LocalDateTime end);
        
    List<Borrowing> findByBookAndStatus(Book book, BorrowingStatus status);
    
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
    
    @Query("SELECT b FROM Borrowing b WHERE b.status = com.library.enums.BorrowingStatus.EN_ATTENTE AND b.dueDate < :now")
    List<Borrowing> findOverdueBorrowings(@Param("now") LocalDateTime now);
    
    @Query("SELECT b FROM Borrowing b WHERE b.dueDate < :date AND b.status = com.library.enums.BorrowingStatus.EN_ATTENTE")
    List<Borrowing> findAllLateLoans(@Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.status = com.library.enums.BorrowingStatus.EN_ATTENTE")
    Long countCurrentBorrowings(@Param("userId") Long userId);
    
    @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.status = com.library.enums.BorrowingStatus.EN_ATTENTE")
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
    
    List<Borrowing> findByUserAndStatus(User user, BorrowingStatus status);
    
    
    List<Borrowing> findByDueDateBeforeAndStatus(LocalDateTime date, BorrowingStatus status);
    
    boolean existsByBookAndStatus(Book book, BorrowingStatus status);
    
    List<Borrowing> findAllByOrderByBorrowDateDesc();
    
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.status = com.library.enums.BorrowingStatus.EMPRUNTE")
    int countActiveBooksByUser(@Param("user") User user);
    
    List<Borrowing> findByUserAndStatusAndDueDateBefore(User user, BorrowingStatus status, LocalDateTime date);
    
    List<Borrowing> findByBookOrderByBorrowDateDesc(Book book);
    
    
    boolean existsByUserAndBookAndStatus(User user, Book book, BorrowingStatus status);
    
    // CORRIGÉ: Changé borrowingDate en borrowDate
    List<Borrowing> findByBorrowDateBetweenOrderByBorrowDateDesc(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Borrowing> findByStatusOrderByBorrowDateAsc(BorrowingStatus status);
    
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, BorrowingStatus status);
    
    @Query("SELECT b FROM Borrowing b WHERE b.user = :user ORDER BY b.borrowDate DESC")
    List<Borrowing> findUserBorrowingHistory(@Param("user") User user);
    
    @Query("SELECT b.user, COUNT(b) as borrowCount FROM Borrowing b GROUP BY b.user ORDER BY borrowCount DESC")
    List<Object[]> findBorrowingStatsByUser();





    // Compter les emprunts avec date de retour (non null)
@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate IS NOT NULL")
long countByReturnDateIsNotNull();

// Trouver les emprunts avec date de retour (non null)
@Query("SELECT b FROM Borrowing b WHERE b.returnDate IS NOT NULL")
List<Borrowing> findByReturnDateIsNotNull();

// Compter les retours dans une plage de dates
@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate BETWEEN :startDate AND :endDate")
long countByReturnDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

// Trouver les retours dans une plage de dates


// Compter les emprunts par jour (pour les 7 derniers jours)
@Query("SELECT COUNT(b) FROM Borrowing b WHERE DATE(b.borrowDate) = :date")
long countByBorrowDate(@Param("date") LocalDate date);

// Statistiques mensuelles des emprunts
@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.borrowDate BETWEEN :startDate AND :endDate")
long countBorrowingsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

// Emprunts retournés à temps (returnDate <= dueDate)
@Query("SELECT b FROM Borrowing b WHERE b.returnDate IS NOT NULL AND b.returnDate <= b.dueDate")
List<Borrowing> findReturnedOnTime();
    @Query("SELECT DISTINCT b.book.category FROM Borrowing b WHERE b.user = :user ORDER BY b.book.category")
    List<String> findDistinctCategoriesByUser(@Param("user") User user);
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.book.category = :category")
    int countByUserAndBookCategory(@Param("user") User user, @Param("category") String category);
    
    @Query("SELECT b FROM Borrowing b WHERE b.user = :user AND b.borrowDate BETWEEN :startDate AND :endDate")
    List<Borrowing> findByUserAndBorrowDateBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT b FROM Borrowing b WHERE b.user = :user AND b.returnDate BETWEEN :startDate AND :endDate")
    List<Borrowing> findByUserAndReturnDateBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT AVG(b.rating) FROM Borrowing b WHERE b.user = :user AND b.book.category = :category AND b.rating IS NOT NULL")
    Double getAverageRatingByUserAndCategory(@Param("user") User user, @Param("category") String category);
        @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.returnDate IS NOT NULL")
    int countByUserAndReturnDateIsNotNull(@Param("user") User user);
    
    @Query("SELECT AVG(b.rating) FROM Borrowing b WHERE b.user = :user AND b.rating IS NOT NULL")
    Double getAverageRatingByUser(@Param("user") User user);
    
    @Query("SELECT AVG(DATEDIFF(b.returnDate, b.borrowDate)) FROM Borrowing b WHERE b.user = :user AND b.returnDate IS NOT NULL")
    Integer getAverageReadingDaysByUser(@Param("user") User user);
    
    @Query("SELECT b.book.category FROM Borrowing b WHERE b.user = :user GROUP BY b.book.category ORDER BY COUNT(b) DESC LIMIT 1")
    String getFavoriteCategoryByUser(@Param("user") User user);


@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.borrowDate BETWEEN :start AND :end")
Long countByBorrowDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

// Emprunts retournés en retard (returnDate > dueDate)
@Query("SELECT b FROM Borrowing b WHERE b.returnDate IS NOT NULL AND b.returnDate > b.dueDate")
List<Borrowing> findReturnedLate();

// Compter les retours à temps
@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate IS NOT NULL AND b.returnDate <= b.dueDate")
long countReturnedOnTime();

// Compter les retours en retard
@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.returnDate IS NOT NULL AND b.returnDate > b.dueDate")
long countReturnedLate();

// Statistiques par statut
@Query("SELECT b.status, COUNT(b) FROM Borrowing b GROUP BY b.status")
List<Object[]> getBorrowingStatusStatistics();

    // Ajoutez cette méthode dans votre BorrowingRepository
@Query("SELECT b FROM Borrowing b WHERE b.returnDate BETWEEN :startDate AND :endDate")
List<Borrowing> findByReturnDateBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
// Ajouter cette méthode

@Query("SELECT COUNT(b) FROM Borrowing b WHERE DATE(b.borrowDate) = DATE(:date)")
Long countByBorrowDateBetween(@Param("date") LocalDate date);

@Query("SELECT b.book.title, b.book.author, COUNT(b) as borrowCount " +
       "FROM Borrowing b " +
       "GROUP BY b.book.title, b.book.author " +
       "ORDER BY borrowCount DESC")

       List<Object[]> findMostBorrowedBooks();
    int countByUserAndReturnDateIsNull(User user);
    List<Borrowing> findByUserAndReturnDateIsNullOrderByBorrowDateDesc(User user);
    @Query("SELECT b.book.title, b.book.author, COUNT(b) as borrowCount " +
       "FROM Borrowing b " +
       "GROUP BY b.book.title, b.book.author " +
       "ORDER BY borrowCount DESC")
    List<Object[]> findTopBorrowedBooks();

@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.returnDate BETWEEN :start AND :end")
int countByUserAndReturnDateBetween(@Param("user") User user, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.dueDate < :date AND b.returnDate IS NULL")
int countByUserAndDueDateBeforeAndReturnDateIsNull(@Param("user") User user, @Param("date") LocalDateTime date);

@Query("SELECT b FROM Borrowing b WHERE b.user = :user AND b.returnDate IS NOT NULL")
List<Borrowing> findByUserAndReturnDateIsNotNull(@Param("user") User user);

@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.returnDate > :date")
int countByUserAndReturnDateAfter(@Param("user") User user, @Param("date") LocalDateTime date);

List<Borrowing> findByUserAndRatingIsNotNull(User user);

@Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user = :user AND b.returnDate <= b.dueDate AND b.returnDate IS NOT NULL")
int countByUserAndReturnDateBeforeOrEqualDueDate(@Param("user") User user);

List<Borrowing> findByStatusAndDueDateBetween(BorrowingStatus status, LocalDateTime start, LocalDateTime end);

// Methods for notification system
@Query("SELECT b FROM Borrowing b WHERE b.dueDate < :now AND b.returnDate IS NULL AND b.overdueNotified = false")
List<Borrowing> findOverdueNotNotified(@Param("now") LocalDateTime now);
@Query("SELECT b FROM Borrowing b WHERE b.dueDate BETWEEN :start AND :end AND b.returnDate IS NULL AND b.dueSoonNotified = false")
List<Borrowing> findDueSoonNotNotified(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

// Method to find borrowings by user and status in a list of statuses
List<Borrowing> findByUserAndStatusIn(User user, List<BorrowingStatus> statuses);

}
    
    


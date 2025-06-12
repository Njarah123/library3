package com.library.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.library.model.Borrow;
import com.library.model.Borrowing;
import com.library.model.User;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByUserId(Long userId);
   List<Borrow> findByStatus(@Param("status") String status);
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
    List<Borrow> findByReturnDateIsNull();
    @Query("SELECT b FROM Borrow b WHERE b.user.id = :userId AND b.status = 'BORROWED'")
    List<Borrow> findCurrentBorrowsByUserId(@Param("userId") Long userId);
    @Query("SELECT b FROM Borrow b JOIN b.user u WHERE u.username = :username AND b.status = 'EMPRUNTÉ'")
    List<Borrow> findCurrentBorrows(@Param("username") String username);
         @Query("SELECT b FROM Borrow b " +
           "JOIN b.user u " +
           "WHERE u.username = :username " +
           "AND EXISTS (SELECT s FROM Staff s WHERE s.id = u.id) " +
           "AND b.status = 'EMPRUNTÉ'")
    List<Borrow> findCurrentBorrowsByStaffUsername(@Param("username") String username);
    List<Borrowing> findByUserAndReturnDateIsNullOrderByBorrowDateDesc(User user);
    int countByUserAndReturnDateIsNull(User user);
}
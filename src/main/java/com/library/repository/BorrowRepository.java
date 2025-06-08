package com.library.repository;

import com.library.model.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    List<Borrow> findByUserId(Long userId);
    List<Borrow> findByStatus(String status);
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, String status);
}
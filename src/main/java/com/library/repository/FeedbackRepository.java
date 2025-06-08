package com.library.repository;

import com.library.model.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findByBookId(Long bookId);
    List<Feedback> findByUserId(Long userId);
    List<Feedback> findByBookIdOrderByCreatedAtDesc(Long bookId);
}
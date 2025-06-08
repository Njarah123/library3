package com.library.service;

import com.library.model.Borrow;
import com.library.repository.BorrowRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;

    public Borrow saveBorrow(Borrow borrow) {
        return borrowRepository.save(borrow);
    }

    public List<Borrow> getBorrowsByUserId(Long userId) {
        return borrowRepository.findByUserId(userId);
    }

    public boolean hasUserBorrowedBook(Long userId, Long bookId) {
        return borrowRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, "BORROWED");
    }

    public List<Borrow> getAllActiveBorrows() {
        return borrowRepository.findByStatus("BORROWED");
    }
}
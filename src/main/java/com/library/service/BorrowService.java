package com.library.service;

import com.library.model.Book;
import com.library.model.Borrow;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowRepository;
import com.library.repository.StaffRepository;
import com.library.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowService {

    @Autowired
    private BorrowRepository borrowRepository;
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private StaffRepository staffRepository; 
@Transactional
    public Borrow createBorrowForStaff(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            
        if (!staffRepository.existsByUserId(user.getId())) {
            throw new RuntimeException("L'utilisateur n'est pas un membre du staff");
        }

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        if (!book.getAvailable()) {
            throw new RuntimeException("Le livre n'est pas disponible");
        }

        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDateTime.now());
        borrow.setExpectedReturnDate(LocalDateTime.now().plusDays(14));
        borrow.setStatus("EMPRUNTÉ");

        book.setAvailable(false);
        bookRepository.save(book);

        return borrowRepository.save(borrow);
    }

    public List<Borrow> getCurrentBorrows() {
        return borrowRepository.findByStatus("EMPRUNTÉ");
    }
      public List<Borrow> getCurrentBorrowsByStaff(String username) {
        return borrowRepository.findCurrentBorrowsByStaffUsername(username);
    }
    public Borrow saveBorrow(Borrow borrow) {
        return borrowRepository.save(borrow);
    }

    public List<Borrow> getBorrowsByUserId(Long userId) {
        return borrowRepository.findByUserId(userId);
    }
     public List<Borrow> getCurrentBorrowsByUser(Long userId) {
        return borrowRepository.findCurrentBorrowsByUserId(userId);
    }

     public List<Borrow> getCurrentBorrows(String username) {
        return borrowRepository.findCurrentBorrows(username);
    }
   
    public boolean hasUserBorrowedBook(Long userId, Long bookId) {
        return borrowRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, "BORROWED");
    }

    public List<Borrow> getAllActiveBorrows() {
        return borrowRepository.findByStatus("BORROWED");
    }

    @Transactional
public void returnBook(Long borrowId) {
    Borrow borrow = borrowRepository.findById(borrowId)
        .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

    if (!"EMPRUNTÉ".equals(borrow.getStatus())) {
        throw new RuntimeException("Ce livre a déjà été retourné");
    }

    borrow.setStatus("RETOURNÉ");
    borrow.setReturnDate(LocalDateTime.now());
    
    // Rendre le livre disponible
    Book book = borrow.getBook();
    book.setAvailable(true);
    bookRepository.save(book);
    
    borrowRepository.save(borrow);
}
}
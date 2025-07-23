package com.library.service;

import com.library.dto.Activity;
import com.library.model.Book;
import com.library.enums.UserType;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BorrowService {

    private static final Logger logger = LoggerFactory.getLogger(BorrowService.class);

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

        book.decrementAvailableQuantity();
        bookRepository.save(book);

        return borrowRepository.save(borrow);
    }

    public List<Borrow> getCurrentBorrows() {
        return borrowRepository.findByStatus("EMPRUNTÉ");
    }
      public List<Borrow> getCurrentBorrowsByStaff(String username) {
        return borrowRepository.findCurrentBorrowsByStaffUsername(username);
    }
    @Transactional
    public Borrow saveBorrow(Borrow borrow) {
        // Si c'est un nouvel emprunt (pas encore sauvegardé), décrémenter la quantité
        if (borrow.getId() == null && "EMPRUNTÉ".equals(borrow.getStatus())) {
            Book book = borrow.getBook();
            if (book != null && book.getAvailableQuantity() > 0) {
                book.decrementAvailableQuantity();
                bookRepository.save(book);
            }
        }
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
        // Vérifier avec les deux statuts possibles pour éviter les incohérences
        return borrowRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, "BORROWED") ||
               borrowRepository.existsByUserIdAndBookIdAndStatus(userId, bookId, "EMPRUNTÉ");
    }

    public List<Borrow> getAllActiveBorrows() {
        return borrowRepository.findByStatus("EMPRUNTÉ");
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
    book.incrementAvailableQuantity();
    bookRepository.save(book);
    
    borrowRepository.save(borrow);
}

    public List<Map<String, Object>> getRecentActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        try {
            List<Borrow> allBorrows = borrowRepository.findActivitiesByStudentAndStaff();
            logger.info("Found {} borrow records for students and staff", allBorrows.size());

            for (Borrow borrow : allBorrows) {
                // Create a borrow activity
                Map<String, Object> borrowActivity = new HashMap<>();
                borrowActivity.put("type", "EMPRUNTÉ");
                borrowActivity.put("description", String.format("%s a emprunté \"%s\"",
                    borrow.getUser().getUsername(), borrow.getBook().getTitle()));
                borrowActivity.put("timestamp", borrow.getBorrowDate());
                activities.add(borrowActivity);

                // If the book has been returned, create a return activity
                if (borrow.getReturnDate() != null) {
                    Map<String, Object> returnActivity = new HashMap<>();
                    returnActivity.put("type", "RETOURNÉ");
                    returnActivity.put("description", String.format("%s a retourné \"%s\"",
                        borrow.getUser().getUsername(), borrow.getBook().getTitle()));
                    returnActivity.put("timestamp", borrow.getReturnDate());
                    activities.add(returnActivity);
                }
            }

            // Sort activities by timestamp in descending order
            activities.sort(Comparator.comparing((Map<String, Object> a) -> (LocalDateTime) a.get("timestamp")).reversed());

        } catch (Exception e) {
            logger.error("Error fetching recent activities", e);
        }
        
        logger.info("Returning {} recent activities", activities.size());
        return activities.stream().limit(10).map(activity -> {
            Map<String, Object> formattedActivity = new HashMap<>(activity);
            LocalDateTime timestamp = (LocalDateTime) formattedActivity.get("timestamp");
            if (timestamp != null) {
                formattedActivity.put("timestamp", timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                formattedActivity.put("timestamp", "Date non disponible");
            }
            return formattedActivity;
        }).collect(Collectors.toList());
    }
}
package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.enums.BorrowingStatus;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;

@Service
public class BorrowingService {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserService userService;

    

 @Transactional
    public void rateBorrowing(Long borrowId, User student, int rating, String comment) {
        Borrowing borrowing = borrowingRepository.findById(borrowId)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        // Vérifier que c'est bien l'emprunt de l'étudiant
        if (!borrowing.getUser().getId().equals(student.getId())) {
            throw new RuntimeException("Vous ne pouvez pas noter cet emprunt");
        }

        // Vérifier que le livre a été retourné
        if (!"RETOURNE".equals(borrowing.getStatus())) {
            throw new RuntimeException("Vous ne pouvez noter que les livres retournés");
        }

        // Vérifier si le livre n'a pas déjà été noté
        if (borrowing.getRating() != null) {
            throw new RuntimeException("Vous avez déjà noté ce livre");
        }

        // Mettre à jour la note de l'emprunt
        borrowing.setRating(rating);
        borrowing.setRatingComment(comment);
        borrowingRepository.save(borrowing);

        // Mettre à jour la note moyenne du livre
        Book book = borrowing.getBook();
        updateBookRating(book, rating);
    }



    public List<Borrowing> getAllBorrowings() {
        return borrowingRepository.findAllWithBooksAndUsers();
    }
    
        public List<Borrowing> getCurrentBorrowings() {
        return borrowingRepository.findByStatusIn(
            Arrays.asList(BorrowingStatus.EN_COURS, BorrowingStatus.EN_RETARD)
        );
    }

 public long countCurrentBorrowings() {
        return borrowingRepository.countByStatus(BorrowingStatus.EN_COURS);
    }

      public long countOverdueBorrowings() {
        LocalDateTime now = LocalDateTime.now();
        return borrowingRepository.countByStatusAndDueDateBefore(BorrowingStatus.EN_COURS, now);
    }

     public long countTodayReturns() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return borrowingRepository.countByStatusAndReturnDateBetween(
            BorrowingStatus.RETOURNE, 
            startOfDay, 
            endOfDay
        );
    }

    

 @Transactional
    public void rateStaffBorrowing(Long borrowId, String staffUsername, int rating, String comment) {
        // Récupérer l'utilisateur staff
        User staff = userService.findByUsername(staffUsername);
        
        Borrowing borrowing = borrowingRepository.findById(borrowId)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        // Vérifier que c'est bien l'emprunt du staff
        if (!borrowing.getUser().getId().equals(staff.getId())) {
            throw new RuntimeException("Vous ne pouvez pas noter cet emprunt");
        }

        // Vérifier que le livre a été retourné
        if (!"RETOURNE".equals(borrowing.getStatus())) {
            throw new RuntimeException("Vous ne pouvez noter que les livres retournés");
        }

        // Vérifier si le livre n'a pas déjà été noté
        if (borrowing.getRating() != null) {
            throw new RuntimeException("Vous avez déjà noté ce livre");
        }

        // Mettre à jour la note de l'emprunt
        borrowing.setRating(rating);
        borrowing.setRatingComment(comment);
        borrowingRepository.save(borrowing);

        // Mettre à jour la note moyenne du livre
        Book book = borrowing.getBook();
        updateBookRating(book, rating);
    }


      public List<Borrowing> getBorrowingsWithFilters(String status) {
        if (status == null || status.isEmpty()) {
            return getAllBorrowings();
        }
        
        BorrowingStatus borrowingStatus = BorrowingStatus.valueOf(status);
        return borrowingRepository.findByStatus(borrowingStatus);
    }


    
private void updateBookRating(Book book, int newRating) {
        Double currentRating = book.getRating();
        Integer numberOfRatings = book.getNumberOfRatings();

        if (currentRating == null || numberOfRatings == null) {
            book.setRating((double) newRating);
            book.setNumberOfRatings(1);
        } else {
            // Calculer la nouvelle moyenne
            double totalRating = currentRating * numberOfRatings + newRating;
            book.setRating(totalRating / (numberOfRatings + 1));
            book.setNumberOfRatings(numberOfRatings + 1);
        }

        bookRepository.save(book);
    }



    // Méthodes existantes pour les emprunts actuels
    public List<Borrowing> getCurrentUserBorrowings() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return borrowingRepository.findByUserOrderByBorrowDateDesc(user);
    }

    // Méthode pour les réservations actuelles
    public List<Reservation> getCurrentUserReservations() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return reservationRepository.findByUserOrderByReservationDateDesc(user);
    }

    // Méthode pour obtenir les amendes
    public BigDecimal getCurrentUserFines() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        return user.getAccount().getFineAmount();
    }

    // Méthode de création d'emprunt
    @Transactional
    public Borrowing createBorrowing(Long bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Vérifier si l'utilisateur peut emprunter
        int currentBorrowings = getCurrentBorrowingsCount(user);
        if (currentBorrowings >= 3) {
            throw new RuntimeException("Vous avez atteint la limite maximale d'emprunts (3)");
        }

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        if (!book.getAvailable()) {
            throw new RuntimeException("Ce livre n'est pas disponible actuellement");
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(14)); // 14 jours pour les étudiants
        borrowing.setStatus("EN_COURS");

        book.setAvailable(false);
        bookRepository.save(book);

        return borrowingRepository.save(borrowing);
    }

    // Méthode de création de réservation
    @Transactional
    public Reservation createReservation(Long bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        if (reservationRepository.countByUserAndStatus(user, "EN_ATTENTE") >= user.getMaxReservationLimit()) {
            throw new RuntimeException("Vous avez atteint votre limite de réservations");
        }

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setBook(book);
        reservation.setReservationDate(LocalDateTime.now());
        reservation.setStatus("EN_ATTENTE");

        return reservationRepository.save(reservation);
    }




    
 @Transactional
    public Borrowing borrowBook(Long bookId, String username) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        if (!book.getAvailable()) {
            throw new RuntimeException("Le livre n'est pas disponible");
        }

        User user = userService.findByUsername(username);
        
        Borrowing borrowing = new Borrowing();
        borrowing.setBook(book);
        borrowing.setUser(user);
        borrowing.setBorrowDate(LocalDateTime.now());
        // 30 jours pour le staff au lieu de 14
        borrowing.setDueDate(LocalDateTime.now().plusDays(30));
        borrowing.setStatus("EN_COURS");

        book.setAvailable(false);
        bookRepository.save(book);
        
        return borrowingRepository.save(borrowing);
    }

    // Ajouter méthode pour obtenir l'historique des emprunts
    public List<Borrowing> getUserBorrowingHistory(String username) {
        User user = userService.findByUsername(username);
        return borrowingRepository.findByUserOrderByBorrowDateDesc(user);
    }

    // Méthode de traitement du retour
    @Transactional
    public void processBookReturn(Long borrowingId) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        Book book = borrowing.getBook();
        
        borrowing.setStatus("RETOURNE");
        borrowing.setReturnDate(LocalDateTime.now());
        borrowingRepository.save(borrowing);

        List<Reservation> pendingReservations = reservationRepository
            .findByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.EN_ATTENTE);

        if (!pendingReservations.isEmpty()) {
            Reservation nextReservation = pendingReservations.get(0);
            nextReservation.setStatus(ReservationStatus.PRET);
            reservationRepository.save(nextReservation);
        } else {
            book.incrementAvailableQuantity();
            bookRepository.save(book);
        }
    }

    // Nouvelles méthodes pour le StudentController
    public int getCurrentBorrowingsCount(User student) {
        return borrowingRepository.countByUserAndStatus(student, "EN_COURS");
    }

    public int getOverdueBorrowingsCount(User student) {
        LocalDateTime now = LocalDateTime.now();
        return borrowingRepository.countByUserAndStatusAndDueDateBefore(student, "EN_COURS", now);
    }

    public int getTotalBorrowingsCount(User student) {
        return borrowingRepository.countByUser(student);
    }

    public List<Borrowing> getUpcomingReturns(User student) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeDaysFromNow = now.plusDays(3);
        return borrowingRepository.findByUserAndStatusAndDueDateBetween(
            student, "EN_COURS", now, threeDaysFromNow);
    }

    public List<Borrowing> getCurrentBorrowings(User student) {
        return borrowingRepository.findByUserAndStatus(student, "EN_COURS");
    }

    public List<Borrowing> getBorrowingHistory(User student) {
        return borrowingRepository.findByUserOrderByBorrowDateDesc(student);
    }

    public int getUniqueBorrowedBooksCount(User student) {
        return borrowingRepository.countDistinctBookByUser(student);
    }

    public int getTotalOverduesCount(User student) {
        return borrowingRepository.countByUserAndReturnDateAfterDueDate(student);
    }

    public int getTotalRatingsCount(User student) {
        return borrowingRepository.countByUserAndRatingIsNotNull(student);
    }

    // Méthode pour emprunter un livre (version étudiant)
    @Transactional
    public void borrowBook(User student, Long bookId) {
        if (!student.canBorrowBooks()) {
            throw new RuntimeException("Vous ne pouvez pas emprunter plus de livres ou vous avez des amendes impayées");
        }

        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new RuntimeException("Livre non trouvé"));

        if (!book.getAvailable()) {
            throw new RuntimeException("Ce livre n'est pas disponible actuellement");
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setUser(student);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDateTime.now());
        borrowing.setDueDate(LocalDateTime.now().plusDays(student.isStaff() ? 30 : 14));
        borrowing.setStatus("EN_COURS");

        book.setAvailable(false);
        bookRepository.save(book);
        borrowingRepository.save(borrowing);
    }

    public void returnBook(Long id) {
        processBookReturn(id);
    }
}
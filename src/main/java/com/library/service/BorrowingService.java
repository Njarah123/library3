package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.library.dto.MemberBorrowingStats;
import com.library.enums.BorrowingStatus;
import com.library.model.Book;
import com.library.model.Borrowing;
import com.library.model.Reservation;
import com.library.model.User;
import com.library.repository.BookRepository;
import com.library.repository.BorrowingRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.UserRepository;
@Service
public class BorrowingService {

     
    @Autowired
    private BorrowingRepository borrowingRepository;

    public Page<Borrowing> getAllBorrowings(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "borrowDate"));
        return borrowingRepository.findAllBorrowings(pageRequest);
    }

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private UserService userService;

 private static final int PAGE_SIZE = 12;
  private static final Logger logger = LoggerFactory.getLogger(BorrowingService.class);

     
public List<Map<String, Object>> getRecentActivities() {
        // Récupérer les 10 dernières activités
        List<Borrowing> recentBorrowings = borrowingRepository.findTop10ByOrderByBorrowingDateDesc();
        List<Map<String, Object>> activities = new ArrayList<>();

        for (Borrowing borrowing : recentBorrowings) {
            Map<String, Object> activity = new HashMap<>();
            
            // Déterminer le type d'activité
            String type = borrowing.getStatus();
            String description;
            LocalDateTime timestamp;

            if ("RETOURNE".equals(type)) {
                description = String.format("%s a retourné \"%s\"", 
                    borrowing.getUser().getUsername(), 
                    borrowing.getBook().getTitle());
                timestamp = borrowing.getReturnDate();
            } else {
                description = String.format("%s a emprunté \"%s\"", 
                    borrowing.getUser().getUsername(), 
                    borrowing.getBook().getTitle());
                timestamp = borrowing.getBorrowingDate();
            }

            activity.put("type", type);
            activity.put("description", description);
            activity.put("timestamp", timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            activities.add(activity);
        }

        return activities;
    }
    


    public List<MemberBorrowingStats> getMemberBorrowingStats() {
        List<MemberBorrowingStats> stats = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        try {
            List<User> users = userRepository.findAll();
            for (User user : users) {
                MemberBorrowingStats memberStats = borrowingRepository.getUserStats(user, now);
                if (memberStats != null) {
                    stats.add(memberStats);
                }
            }
        } catch (Exception e) {
            logger.error("Erreur lors de la génération des statistiques des membres", e);
        }
        
        return stats;
    }
  public Long getCurrentlyBorrowedCount() {
        try {
            return borrowingRepository.countByReturnDateIsNull();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des emprunts en cours", e);
            return 0L;
        }
    
    }
       public List<Borrowing> getOverdueLoans() {
        try {
            return borrowingRepository.findOverdueBorrowings();
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des emprunts en retard", e);
            return new ArrayList<>();
        }
    }

     @Transactional
    public void returnBook(Long borrowingId, String conditionAfter) {
        try {
            Borrowing borrowing = borrowingRepository.findById(borrowingId)
                    .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));
            borrowing.setReturnDate(LocalDateTime.now());
            borrowing.setStatus("RETOURNE");
            borrowing.setConditionAfter(conditionAfter);
            
            // Calcul des amendes si retard
            if (borrowing.getDueDate().isBefore(LocalDateTime.now())) {
                long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDateTime.now());
                borrowing.setFineAmount(new BigDecimal(daysLate).multiply(new BigDecimal("1.00"))); // 1€ par jour
            }
            
            borrowingRepository.save(borrowing);
        } catch (Exception e) {
            logger.error("Erreur lors du retour du livre", e);
            throw new RuntimeException("Erreur lors du retour du livre", e);
        }
    }

     @Transactional
    public void renewBorrowing(Long borrowingId) {
        try {
            Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

            if (borrowing.getRenewalCount() >= 3) {
                throw new RuntimeException("Nombre maximum de renouvellements atteint");
            }

            borrowing.setRenewalCount(borrowing.getRenewalCount() + 1);
            borrowing.setLastRenewalDate(LocalDateTime.now());
            borrowing.setDueDate(borrowing.getDueDate().plusDays(14));

            borrowingRepository.save(borrowing);
        } catch (Exception e) {
            logger.error("Erreur lors du renouvellement de l'emprunt", e);
            throw new RuntimeException("Erreur lors du renouvellement", e);
        }
    }
    @Transactional
    public Borrowing createBorrowing(Long userId, Long bookId) {
        try {
            Borrowing borrowing = new Borrowing();
            // Initialisation des champs de l'emprunt
            borrowing.setBorrowDate(LocalDateTime.now());
            borrowing.setBorrowingDate(LocalDateTime.now());
            borrowing.setDueDate(LocalDateTime.now().plusDays(14));
            borrowing.setStatus("EN_ATTENTE");
            borrowing.setFineAmount(BigDecimal.ZERO);
            borrowing.setRenewalCount(0);
            
            // Sauvegarde et retour
            return borrowingRepository.save(borrowing);
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'emprunt", e);
            throw new RuntimeException("Erreur lors de la création de l'emprunt", e);
        }
    }
     public List<Borrowing> getAllLateLoans() {
        try {
            return borrowingRepository.findAllLateLoans(LocalDateTime.now());
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de tous les emprunts en retard", e);
            return new ArrayList<>();
        }
    }

     @Transactional
    public void renewLoan(Long borrowingId) {
        try {
            Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));
            
            if (borrowing.getRenewalCount() >= 3) {
                throw new RuntimeException("Nombre maximum de renouvellements atteint");
            }
            
            borrowing.setRenewalCount(borrowing.getRenewalCount() + 1);
            borrowing.setLastRenewalDate(LocalDateTime.now());
            borrowing.setDueDate(borrowing.getDueDate().plusDays(14)); // exemple: +14 jours
            
            borrowingRepository.save(borrowing);
        } catch (Exception e) {
            logger.error("Erreur lors du renouvellement de l'emprunt", e);
            throw new RuntimeException("Erreur lors du renouvellement", e);
        }
    }

    @Transactional
    public void updateBorrowingStatus(Long borrowingId, String newStatus) {
        try {
            Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));
            
            borrowing.setStatus(newStatus);
            
            if ("RETOURNE".equals(newStatus)) {
                borrowing.setReturnDate(LocalDateTime.now());
                
                // Calcul des amendes si retard
                if (borrowing.getDueDate().isBefore(LocalDateTime.now())) {
                    long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDateTime.now());
                    borrowing.setFineAmount(new BigDecimal(daysLate).multiply(new BigDecimal("1.00")));
                }
            }
            
            borrowingRepository.save(borrowing);
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du statut", e);
            throw new RuntimeException("Erreur lors de la mise à jour du statut", e);
        }
    }
     public Long countUserBorrowings(Long userId) {
        try {
            return borrowingRepository.countByUserId(userId);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des emprunts de l'utilisateur", e);
            return 0L;
        }
    }

    public List<Map<String, Object>> getOverdueBooks() {
        // Récupérer tous les emprunts en retard
        LocalDateTime now = LocalDateTime.now();
        List<Borrowing> overdueBooks = borrowingRepository.findByStatusAndDueDateBefore("EMPRUNTE", now);
        List<Map<String, Object>> overdueList = new ArrayList<>();

        for (Borrowing borrowing : overdueBooks) {
            Map<String, Object> overdue = new HashMap<>();
            
            Book book = borrowing.getBook();
            User borrower = borrowing.getUser();

            overdue.put("title", book.getTitle());
            overdue.put("imagePath", book.getImagePath() != null ? book.getImagePath() : "/images/default-book.png");
            overdue.put("borrower", Map.of(
                "name", borrower.getName(),
                "email", borrower.getEmail()
            ));
            overdue.put("dueDate", borrowing.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            overdue.put("daysOverdue", ChronoUnit.DAYS.between(borrowing.getDueDate(), now));
            
            overdueList.add(overdue);
        }

        return overdueList;
    }



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

    public Page<Borrowing> getAllBorrowingsWithPagination(Pageable pageable) {
        logger.info("Récupération de tous les emprunts avec pagination");
        return borrowingRepository.findAllByOrderByBorrowDateDesc(pageable);
    }


    public List<Borrowing> getAllBorrowings() {
        return borrowingRepository.findAllWithBooksAndUsers();
    }
    
        public List<Borrowing> getCurrentBorrowings() {
        return borrowingRepository.findByStatusIn(
            Arrays.asList(BorrowingStatus.EN_COURS, BorrowingStatus.EN_RETARD)
        );
    }

    public List<Borrowing> getOverdueBorrowings(Long userId, LocalDateTime date) {
        try {
            return borrowingRepository.findByUserIdAndDueDateBefore(userId, date);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche des emprunts en retard", e);
            return new ArrayList<>();
        }}
         public Long countOverdueBorrowings(Long userId, LocalDateTime date) {
        try {
            return borrowingRepository.countLateLoans(userId, date);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des emprunts en retard", e);
            return 0L;
        }
    }
    
 public Long countLateLoans(Long userId, LocalDateTime date) {
        try {
            return borrowingRepository.countLateLoans(userId, date);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des emprunts en retard pour l'utilisateur {}", userId, e);
            return 0L;
        }
    }
  public List<Borrowing> getLateLoans(Long userId, LocalDateTime date) {
        try {
            return borrowingRepository.findLateLoans(userId, date);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des emprunts en retard pour l'utilisateur {}", userId, e);
            return new ArrayList<>();
        }
    }
     public Long getCurrentBorrowingsCount(Long userId) {
        try {
            return borrowingRepository.countCurrentBorrowings(userId);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des emprunts actuels pour l'utilisateur {}", userId, e);
            return 0L;
        }
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

    public Page<Borrowing> getAllBorrowings(int page) {
        return borrowingRepository.findAll(
            PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "borrowDate"))
        );
    }

    public int getTotalPages() {
        long totalElements = getTotalBorrowings();
        return (int) Math.ceil((double) totalElements / PAGE_SIZE);
    }

     public Page<Borrowing> getBorrowingsByStatus(String status, int page) {
        return borrowingRepository.findByStatus(
            status,
            PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "borrowDate"))
        );
    }

     public Page<Borrowing> getUserBorrowingHistory(User user, int page) {
        return borrowingRepository.findByUser(
            user,
            PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "borrowDate"))
        );
    }

      public long getTotalBorrowings() {
        return borrowingRepository.count();
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
 public Long getUserBorrowingsCount(Long userId) {
        try {
            return borrowingRepository.findBorrowingsCountByUserId(userId);
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des emprunts de l'utilisateur", e);
            return 0L;
        }}


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

 @Transactional
public void processBookReturn(Long borrowId) {
    logger.info("Traitement du retour pour l'emprunt: {}", borrowId);
    
    Borrowing borrowing = borrowingRepository.findById(borrowId)
        .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

    if (borrowing.getReturnDate() != null) {
        throw new RuntimeException("Ce livre a déjà été retourné");
    }

    // Mettre à jour l'emprunt avec la date formatée en UTC
    LocalDateTime currentDateTime = LocalDateTime.now(ZoneOffset.UTC);
    String formattedDate = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    LocalDateTime parsedDateTime = LocalDateTime.parse(formattedDate, 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    
    borrowing.setReturnDate(parsedDateTime);
    
    // Mettre à jour le livre
    Book book = borrowing.getBook();
    if (book == null) {
        throw new RuntimeException("Le livre associé à cet emprunt n'existe pas");
    }
    
    book.setAvailable(true);
    bookRepository.save(book);
    borrowingRepository.save(borrowing);
    
    logger.info("Retour traité avec succès pour l'emprunt: {} à {}", borrowId, formattedDate);
}

    // Nouvelles méthodes pour le StudentController
    
    public int getCurrentBorrowingsCount(User user) {
        return borrowingRepository.countByUserAndReturnDateIsNull(user);
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

public List<Borrowing> getCurrentBorrowings(User user) {
        return borrowingRepository.findByUserAndReturnDateIsNullOrderByBorrowDateDesc(user);
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

   @Transactional
    public void returnBook(Long id) {
        logger.info("Début du processus de retour pour l'emprunt ID: {}", id);
        
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));

        // Vérifier si le livre n'est pas déjà retourné
        if (borrowing.getReturnDate() != null) {
            throw new RuntimeException("Ce livre a déjà été retourné");
        }

        // Mettre à jour la date de retour
        borrowing.setReturnDate(LocalDateTime.now());
        
        // Mettre à jour le statut du livre
        Book book = borrowing.getBook();
        book.setAvailable(true);
        
        // Sauvegarder les modifications
        bookRepository.save(book);
        borrowingRepository.save(borrowing);
        
        logger.info("Livre retourné avec succès pour l'emprunt ID: {}", id);
    }
    
}
package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.library.enums.UserType;
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

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private NotificationTriggerService notificationTriggerService;

 private static final int PAGE_SIZE = 12;
  private static final Logger logger = LoggerFactory.getLogger(BorrowingService.class);

     
public List<Map<String, Object>> getRecentActivities() {
    // Récupérer les 10 dernières activités
    List<Borrowing> recentBorrowings = borrowingRepository.findTop10ByOrderByBorrowDateDesc();
    List<Map<String, Object>> activities = new ArrayList<>();
    
    for (Borrowing borrowing : recentBorrowings) {
        Map<String, Object> activity = new HashMap<>();
        
        // Déterminer le type d'activité
        BorrowingStatus type = borrowing.getStatus();
        String description;
        LocalDateTime timestamp;
        
        if (BorrowingStatus.RETOURNE.equals(type)) {
            description = String.format("%s a retourné \"%s\"",
                borrowing.getUser().getUsername(),
                borrowing.getBook().getTitle());
            timestamp = borrowing.getReturnDate();
        } else {
            description = String.format("%s a emprunté \"%s\"",
                borrowing.getUser().getUsername(),
                borrowing.getBook().getTitle());
            timestamp = borrowing.getBorrowDate(); // Changé de getBorrowingDate() à getBorrowDate()
        }
        
        activity.put("type", type);
        activity.put("description", description);
        
        // Vérification que timestamp n'est pas null avant de le formater
        if (timestamp != null) {
            activity.put("timestamp", timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            activity.put("timestamp", "Date non disponible");
        }
        
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
             borrowing.setStatus(BorrowingStatus.RETOURNE);
             borrowing.setConditionAfter(conditionAfter);
             
             // Calcul des amendes si retard
             if (borrowing.getDueDate().isBefore(LocalDateTime.now())) {
                 long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDateTime.now());
                 borrowing.setFineAmount(new BigDecimal(daysLate).multiply(new BigDecimal("1.00"))); // 1€ par jour
             }
             
             // CORRECTION: Restaurer la quantité disponible du livre
             Book book = borrowing.getBook();
             book.incrementAvailableQuantity();
             bookRepository.save(book);
             
             borrowingRepository.save(borrowing);
             
             // Trigger notification for book return
             notificationTriggerService.onBookReturned(borrowing);
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
           // Récupérer l'utilisateur et le livre
           User user = userRepository.findById(userId)
               .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
           Book book = bookRepository.findById(bookId)
               .orElseThrow(() -> new RuntimeException("Livre non trouvé"));
           
           // Vérifier si le livre est disponible
           if (!book.getAvailable()) {
               throw new RuntimeException("Le livre n'est pas disponible");
           }
           
           // Vérifier si l'utilisateur n'a pas dépassé sa limite d'emprunts
           int currentBorrowings = borrowingRepository.countByUserAndReturnDateIsNull(user);
           int maxBorrowings = user.getUserType() == UserType.STAFF ? 10 : 5; // exemple de limites
           
           if (currentBorrowings >= maxBorrowings) {
               throw new RuntimeException("Limite d'emprunts atteinte");
           }
           
           // Créer l'emprunt avec le constructeur qui prend user et book
           Borrowing borrowing = new Borrowing(user, book);
           
           // Les champs suivants sont déjà initialisés dans le constructeur et @PrePersist
           // mais vous pouvez les définir explicitement si nécessaire
           borrowing.setBorrowDate(LocalDateTime.now());
           borrowing.setStatus(BorrowingStatus.EN_COURS); // Utiliser EN_COURS pour les emprunts actifs
           borrowing.setFineAmount(BigDecimal.ZERO);
           borrowing.setRenewalCount(0);
           
           // Marquer le livre comme non disponible
           book.decrementAvailableQuantity();
           
           // Incrémenter le nombre total d'emprunts du livre
           book.setTotalBorrows(book.getTotalBorrows() != null ? book.getTotalBorrows() + 1 : 1);
           bookRepository.save(book);
           
           Borrowing savedBorrowing = borrowingRepository.save(borrowing);
   
           // Notify about the book borrowing
           notificationTriggerService.onBookBorrowed(savedBorrowing);
   
           // Sauvegarde et retour
           return savedBorrowing;
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
    public void updateBorrowingStatus(Long borrowingId, BorrowingStatus newStatus) {
        try {
            Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));
            
            borrowing.setStatus(newStatus);
            
            if (BorrowingStatus.RETOURNE.equals(newStatus)) {
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
        List<Borrowing> overdueBooks = borrowingRepository.findByStatusAndDueDateBefore(BorrowingStatus.EMPRUNTE, now);
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
        if (!BorrowingStatus.RETOURNE.equals(borrowing.getStatus())) {
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

        public long countBorrowingsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
            return borrowingRepository.countByBorrowDateBetween(startDate, endDate);
        }

    public int getTotalBorrowsCount(User user) {
        return borrowingRepository.countByUser(user);
    }
    
    public int getOverdueBorrowsCount(User user) {
        return borrowingRepository.countByUserAndStatusAndDueDateBefore(
            user, BorrowingStatus.EN_ATTENTE, LocalDateTime.now());
    }
    
    public int getCompletedBorrowsCount(User user) {
        return borrowingRepository.countByUserAndReturnDateIsNotNull(user);
    }
    
    public Double getAverageRating(User user) {
        return borrowingRepository.getAverageRatingByUser(user);
    }
    
    public int getAverageReadingDays(User user) {
        return borrowingRepository.getAverageReadingDaysByUser(user);
    }
    
    public String getFavoriteCategory(User user) {
        List<String> categories = borrowingRepository.getFavoriteCategoriesByUser(user);
        return categories.isEmpty() ? null : categories.get(0);
    }

public long countReturnedOnTime() {
    try {
        return borrowingRepository.countReturnedOnTime();
    } catch (Exception e) {
        System.err.println("Erreur lors du comptage des retours à temps: " + e.getMessage());
        // Méthode alternative si la requête échoue
        try {
            List<Borrowing> returned = borrowingRepository.findByReturnDateIsNotNull();
            return returned.stream()
                .filter(b -> b.getReturnDate() != null && b.getDueDate() != null)
                .filter(b -> !b.getReturnDate().isAfter(b.getDueDate()))
                .count();
        } catch (Exception e2) {
            System.err.println("Méthode alternative échouée: " + e2.getMessage());
            return 0L;
        }
    }
}

public long countTotalReturned() {
    try {
        return borrowingRepository.countByReturnDateIsNotNull();
    } catch (Exception e) {
        System.err.println("Erreur lors du comptage des retours totaux: " + e.getMessage());
        return 0L;
    }
}


/**
 * Compter les retours effectués dans une plage de dates
 */
public long countReturnsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
    try {
        System.out.println("=== DEBUG RETOURS ===");
        System.out.println("Recherche des retours entre: " + startDate + " et " + endDate);
        
        // MÉTHODE 1: Utiliser une requête directe si elle existe
        // Vérifiez si cette méthode existe dans votre BorrowingRepository
        try {
            List<Borrowing> todayReturns = borrowingRepository.findByReturnDateBetween(startDate, endDate);
            if (todayReturns != null) {
                System.out.println("Méthode 1 - Retours trouvés: " + todayReturns.size());
                return todayReturns.size();
            }
        } catch (Exception e) {
            System.out.println("Méthode 1 échouée, passage à la méthode 2");
        }
        
        // MÉTHODE 2: Récupérer tous les emprunts et filtrer
        List<Borrowing> allBorrowings = borrowingRepository.findAll();
        System.out.println("Total emprunts dans la DB: " + (allBorrowings != null ? allBorrowings.size() : 0));
        
        if (allBorrowings == null || allBorrowings.isEmpty()) {
            System.out.println("Aucun emprunt trouvé dans la base");
            return 0L;
        }
        
        // Filtrer les emprunts retournés aujourd'hui
        long count = allBorrowings.stream()
            .filter(b -> {
                boolean hasReturnDate = b.getReturnDate() != null;
                if (hasReturnDate) {
                    boolean isInRange = !b.getReturnDate().isBefore(startDate) && !b.getReturnDate().isAfter(endDate);
                    if (isInRange) {
                        System.out.println("Retour trouvé: " + b.getId() + " - " + b.getReturnDate());
                    }
                    return isInRange;
                }
                return false;
            })
            .count();
            
        System.out.println("Nombre de retours aujourd'hui: " + count);
        System.out.println("=== FIN DEBUG RETOURS ===");
        
        return count;
        
    } catch (Exception e) {
        System.err.println("Erreur lors du comptage des retours par date: " + e.getMessage());
        e.printStackTrace();
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

     // Remplacez cette méthode dans votre BorrowingService
public long countOverdueBorrowings() {
    try {
        LocalDateTime now = LocalDateTime.now();
        List<Borrowing> overdueBorrowings = borrowingRepository.findByReturnDateIsNullAndDueDateBefore(now);
        return overdueBorrowings != null ? overdueBorrowings.size() : 0L;
    } catch (Exception e) {
        System.err.println("Erreur lors du comptage des emprunts en retard: " + e.getMessage());
        return 0L;
    }
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

     public Page<Borrowing> getBorrowingsByStatus(BorrowingStatus status, int page) {
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
        if (!BorrowingStatus.RETOURNE.equals(borrowing.getStatus())) {
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

public List<Long> getBorrowedBookIdsByUser(User user) {
    return borrowingRepository.findByUserOrderByBorrowDateDesc(user).stream()
        .map(borrowing -> borrowing.getBook().getId())
        .distinct()
        .collect(Collectors.toList());
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
        int currentBorrowings = borrowingRepository.countByUserAndReturnDateIsNull(user);
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
        borrowing.setStatus(BorrowingStatus.EN_COURS);

        book.decrementAvailableQuantity();
        // Incrémenter le nombre total d'emprunts du livre
        book.setTotalBorrows(book.getTotalBorrows() != null ? book.getTotalBorrows() + 1 : 1);
        bookRepository.save(book);

        Borrowing savedBorrowing = borrowingRepository.save(borrowing);
        
        return savedBorrowing;
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
    
    /**
     * Checks for books that are overdue or due soon and sends notifications to users
     */
    @Transactional
    public void checkAndNotifyDueBooks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime soonDueThreshold = now.plusDays(2); // Books due in 2 days
        
        // Find overdue books
        List<Borrowing> overdueBooks = borrowingRepository.findByStatusAndDueDateBefore(
            BorrowingStatus.EMPRUNTE, now);
            
        // Find books due soon (assuming this repository method exists)
        List<Borrowing> dueSoonBooks = borrowingRepository.findByStatusAndDueDateBetween(
            BorrowingStatus.EMPRUNTE, now, soonDueThreshold);
            
        // Send notifications for overdue books
        for (Borrowing borrowing : overdueBooks) {
            notificationTriggerService.onBookOverdue(borrowing);
        }
        
        // Send notifications for books due soon
        for (Borrowing borrowing : dueSoonBooks) {
            notificationTriggerService.onBookDueSoon(borrowing);
        }
        
        // Send book recommendations once a week (on Monday)
        if (now.getDayOfWeek().getValue() == 1) { // Monday
            sendBookRecommendations();
        }
    }
    
    /**
     * Sends book recommendations to students and staff based on their borrowing history
     */
    @Transactional
    public void sendBookRecommendations() {
        logger.info("Sending book recommendations to students and staff");
        try {
            // Get all students and staff
            List<User> students = userService.findByUserType(UserType.STUDENT);
            List<User> staff = userService.findByUserType(UserType.STAFF);
            
            // Combine the lists
            List<User> users = new ArrayList<>();
            users.addAll(students);
            users.addAll(staff);
            
            // For each user, find books they might be interested in
            for (User user : users) {
                // Get the user's favorite category based on borrowing history
                String favoriteCategory = getFavoriteCategory(user);
                
                if (favoriteCategory != null && !favoriteCategory.isEmpty()) {
                    // Find books in the user's favorite category that they haven't borrowed yet
                    List<Long> borrowedBookIds = getBorrowedBookIdsByUser(user);
                    List<String> categories = Arrays.asList(favoriteCategory);
                    List<Book> recommendedBooks = bookRepository.findRecommendedBooksByCategories(
                        categories,
                        borrowedBookIds,
                        PageRequest.of(0, 1)
                    );
                    
                    // Send a recommendation for one book
                    if (!recommendedBooks.isEmpty()) {
                        Book recommendedBook = recommendedBooks.get(0);
                        String reason = "Basé sur votre intérêt pour la catégorie '" + favoriteCategory + "'";
                        notificationTriggerService.sendBookRecommendation(user, recommendedBook, reason);
                        logger.info("Sent book recommendation to user {}: {}", user.getUsername(), recommendedBook.getTitle());
                    }
                }
            }
            
            logger.info("Book recommendations sent successfully");
        } catch (Exception e) {
            logger.error("Error sending book recommendations", e);
        }
    }
    
    // Helper methods for controllers that are referenced in other files
    
    public int getCurrentBorrowingsCount(User user) {
        return borrowingRepository.countByUserAndReturnDateIsNull(user);
    }
    
    public int getOverdueBorrowingsCount(User user) {
        LocalDateTime now = LocalDateTime.now();
        return borrowingRepository.countByUserAndStatusAndDueDateBefore(
            user, BorrowingStatus.EMPRUNTE, now);
    }
    
    public int getTotalBorrowingsCount(User user) {
        return borrowingRepository.countByUser(user);
    }
    
    public Double getAverageRatingByUser(User user) {
        return borrowingRepository.getAverageRatingByUser(user);
    }
    
    public List<Borrowing> getCurrentBorrowingsByUser(User user) {
        return borrowingRepository.findByUserAndReturnDateIsNull(user);
    }
    
    public List<Borrowing> getRecentBorrowingsByUser(User user, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "borrowDate"));
        Page<Borrowing> page = borrowingRepository.findByUser(user, pageRequest);
        return page.getContent();
    }
    
    public List<Borrowing> getRecentReturnsByUser(User user, int limit) {
        // Implement a simpler version that doesn't require a new repository method
        List<Borrowing> allReturns = borrowingRepository.findByUserAndReturnDateIsNotNull(user);
        return allReturns.stream()
            .sorted((b1, b2) -> b2.getReturnDate().compareTo(b1.getReturnDate()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public List<Borrowing> getBorrowingHistory(User user) {
        return borrowingRepository.findByUserOrderByBorrowDateDesc(user);
    }
    
    public int getTotalOverduesCount(User user) {
        LocalDateTime now = LocalDateTime.now();
        return borrowingRepository.countByUserAndStatusAndDueDateBefore(
            user, BorrowingStatus.EMPRUNTE, now);
    }
    
    public int getUniqueBorrowedBooksCount(User user) {
        // Implement without requiring a new repository method
        List<Borrowing> borrowings = borrowingRepository.findByUserOrderByBorrowDateDesc(user);
        return (int) borrowings.stream()
            .map(b -> b.getBook().getId())
            .distinct()
            .count();
    }
    
    public List<Borrowing> getCurrentBorrowings(User user) {
        logger.info("Getting current borrowings for user: {}", user.getUsername());
        List<Borrowing> borrowings = new ArrayList<>();
        
        try {
            // First try to get borrowings with null returnDate
            try {
                List<Borrowing> nullReturnDateBorrowings = borrowingRepository.findByUserAndReturnDateIsNull(user);
                if (nullReturnDateBorrowings != null) {
                    borrowings.addAll(nullReturnDateBorrowings);
                    logger.info("Found {} borrowings with null returnDate", nullReturnDateBorrowings.size());
                }
            } catch (Exception e) {
                logger.error("Error getting borrowings with null returnDate: {}", e.getMessage());
            }
            
            // Also check for borrowings with specific statuses
            List<BorrowingStatus> activeStatuses = Arrays.asList(
                BorrowingStatus.EMPRUNTE,
                BorrowingStatus.EN_COURS,
                BorrowingStatus.EN_ATTENTE,
                BorrowingStatus.EN_RETARD
            );
            
            // Try to get borrowings by each status individually to avoid potential issues with the IN clause
            for (BorrowingStatus status : activeStatuses) {
                try {
                    List<Borrowing> statusBorrows = borrowingRepository.findByUserAndStatus(user, status);
                    if (statusBorrows != null) {
                        borrowings.addAll(statusBorrows);
                        logger.info("Found {} borrowings with status {}", statusBorrows.size(), status);
                    }
                } catch (Exception e) {
                    logger.error("Error getting borrowings with status {}: {}", status, e.getMessage());
                }
            }
            
            // Remove duplicates
            List<Borrowing> uniqueBorrowings = borrowings.stream()
                .distinct()
                .collect(Collectors.toList());
                
            logger.info("Returning {} unique current borrowings", uniqueBorrowings.size());
            
            // Ensure book data is eagerly loaded
            for (Borrowing borrowing : uniqueBorrowings) {
                try {
                    if (borrowing.getBook() != null) {
                        // Access book properties to ensure they're loaded
                        Book book = borrowing.getBook();
                        book.getTitle(); // Force loading
                        book.getAuthor(); // Force loading
                        
                        // Ensure image paths are set
                        if (book.getCoverImageUrl() == null && book.getImagePath() == null) {
                            book.setImagePath("/images/default-book.png");
                        } else if (book.getImagePath() == null && book.getCoverImageUrl() != null) {
                            book.setImagePath(book.getCoverImageUrl());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error loading book data for borrowing {}: {}", borrowing.getId(), e.getMessage());
                }
            }
            
            return uniqueBorrowings;
        } catch (Exception e) {
            logger.error("Error getting current borrowings: {}", e.getMessage(), e);
            return new ArrayList<>(); // Return empty list instead of null
        }
    }
    
    @Transactional
    public void processBookReturn(Long borrowId) {
        Borrowing borrowing = borrowingRepository.findById(borrowId)
            .orElseThrow(() -> new RuntimeException("Emprunt non trouvé"));
        
        returnBook(borrowId, "Good");
    }
    
    public int getOverdueBooksCount(User user) {
        LocalDateTime now = LocalDateTime.now();
        return borrowingRepository.countByUserAndStatusAndDueDateBefore(
            user, BorrowingStatus.EMPRUNTE, now);
    }
    
    public int getBooksReadThisMonth(User user) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        // Implement without requiring a new repository method
        List<Borrowing> allReturns = borrowingRepository.findByUserAndReturnDateIsNotNull(user);
        return (int) allReturns.stream()
            .filter(b -> b.getReturnDate().isAfter(startOfMonth))
            .count();
    }
    
    public List<Integer> getWeeklyReadingData(User user) {
        // Implementation for weekly reading data
        List<Integer> weeklyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        List<Borrowing> allReturns = borrowingRepository.findByUserAndReturnDateIsNotNull(user);
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime dayStart = now.minusDays(i).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime dayEnd = dayStart.plusDays(1);
            int count = (int) allReturns.stream()
                .filter(b -> b.getReturnDate().isAfter(dayStart) && b.getReturnDate().isBefore(dayEnd))
                .count();
            weeklyData.add(count);
        }
        
        return weeklyData;
    }
    
    public List<Integer> getMonthlyReadingData(User user) {
        // Implementation for monthly reading data
        List<Integer> monthlyData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        List<Borrowing> allReturns = borrowingRepository.findByUserAndReturnDateIsNotNull(user);
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1);
            int count = (int) allReturns.stream()
                .filter(b -> b.getReturnDate().isAfter(monthStart) && b.getReturnDate().isBefore(monthEnd))
                .count();
            monthlyData.add(count);
        }
        
        return monthlyData;
    }
    
    public Map<String, Long> getCategoryPreferences(User user) {
        // Implement without requiring a new repository method
        List<Borrowing> borrowings = borrowingRepository.findByUserOrderByBorrowDateDesc(user);
        return borrowings.stream()
            .map(b -> b.getBook().getCategory())
            .filter(category -> category != null && !category.isEmpty())
            .collect(Collectors.groupingBy(category -> category, Collectors.counting()));
    }
    
    public int getWeeklyReadingCount(User user) {
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0);
        // Implement without requiring a new repository method
        List<Borrowing> allReturns = borrowingRepository.findByUserAndReturnDateIsNotNull(user);
        return (int) allReturns.stream()
            .filter(b -> b.getReturnDate().isAfter(weekStart))
            .count();
    }
    
    public int getTotalBooksReadByUser(User user) {
        return borrowingRepository.countByUserAndReturnDateIsNotNull(user);
    }
    
    public int getOnTimeReturnsCount(User user) {
        // Implement without requiring a new repository method
        List<Borrowing> allReturns = borrowingRepository.findByUserAndReturnDateIsNotNull(user);
        return (int) allReturns.stream()
            .filter(b -> b.getReturnDate().isBefore(b.getDueDate()))
            .count();
    }
    
    public int getReviewsCount(User user) {
        // Implement without requiring a new repository method
        List<Borrowing> borrowings = borrowingRepository.findByUserOrderByBorrowDateDesc(user);
        return (int) borrowings.stream()
            .filter(b -> b.getRating() != null)
            .count();
    }
    
    public List<Borrowing> getUserBorrowingHistory(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return borrowingRepository.findByUserOrderByBorrowDateDesc(user);
    }
    
    public void borrowBook(Long bookId, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Borrowing borrowing = createBorrowing(user.getId(), bookId);
    }
    
    public long getTodayBorrowingsCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return borrowingRepository.countByBorrowDateBetween(startOfDay, endOfDay);
    }
    
    public long getTodayReturnsCount() {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return borrowingRepository.countByReturnDateBetween(startOfDay, endOfDay);
    }
    
    /**
     * Updates the status of a borrowing record
     * @param borrowing The borrowing record to update
     * @param status The new status
     * @return The updated borrowing record
     */
    @Transactional
    public Borrowing updateBorrowingStatus(Borrowing borrowing, BorrowingStatus status) {
        borrowing.setStatus(status);
        return borrowingRepository.save(borrowing);
    }
    
    /**
     * Sauvegarde un emprunt dans la base de données
     * @param borrowing L'emprunt à sauvegarder
     * @return L'emprunt sauvegardé
     */
    @Transactional
    public Borrowing saveBorrowing(Borrowing borrowing) {
        return borrowingRepository.save(borrowing);
    }
}
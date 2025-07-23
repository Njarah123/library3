package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.library.enums.BorrowingStatus;
import com.library.enums.UserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"book", "user"})
@ToString(exclude = {"book", "user"})
@Entity
@Table(name = "borrowings")
public class Borrowing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime borrowDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;
    
    private LocalDateTime returnDate;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BorrowingStatus status;

    // Supprimé borrowingDate car redondant avec borrowDate
    // @Column(name = "borrowing_date")
    // private LocalDateTime borrowingDate;

    @Column(precision = 10, scale = 2)
    private BigDecimal fineAmount;

    private String conditionBefore;
    private String conditionAfter;

    private int renewalCount = 0;
    private LocalDateTime lastRenewalDate;
    
    // Notification tracking fields
    private boolean overdueNotified = false;
    private boolean dueSoonNotified = false;

    // Champs pour les évaluations
    @Column(name = "rating")
    private Integer rating;

    @Column(name = "rating_comment", length = 500)
    private String ratingComment;

    // Constructeur par défaut avec initialisation de la date actuelle
    public Borrowing() {
        this.borrowDate = LocalDateTime.now();
        this.fineAmount = BigDecimal.ZERO;
    }
    
    // Constructeur avec user et book
    public Borrowing(User user, Book book) {
        this();
        this.setUser(user);
        this.setBook(book);
        this.dueDate = this.borrowDate.plusDays(user.getUserType() == UserType.STAFF ? 30 : 14);
    }

    // Méthodes de gestion des relations
    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getBorrowings() != null && !user.getBorrowings().contains(this)) {
            user.getBorrowings().add(this);
        }
    }

    public void setBook(Book book) {
        this.book = book;
        if (book != null && !book.getAvailable()) {
            throw new IllegalStateException("Le livre n'est pas disponible");
        }
    }

    // Méthodes de gestion du statut et des renouvellements
    public boolean canBeRenewed() {
        return BorrowingStatus.EMPRUNTE.equals(status)
            && renewalCount < getMaxRenewals()
            && !isOverdue();
    }

    public void renew() {
        if (!canBeRenewed()) {
            throw new IllegalStateException("Cet emprunt ne peut pas être renouvelé");
        }
        
        this.renewalCount++;
        this.lastRenewalDate = LocalDateTime.now();
        this.dueDate = this.dueDate.plusDays(14); // Ajoute 14 jours à la date de retour
    }

    public boolean isOverdue() {
        return BorrowingStatus.EMPRUNTE.equals(status)
            && LocalDateTime.now().isAfter(dueDate);
    }

    private int getMaxRenewals() {
        return user != null && user.getUserType() == UserType.STAFF ? 2 : 1;
    }

    // Méthodes de gestion des dates
    public void setApprovalDate(LocalDateTime now) {
        if (BorrowingStatus.EN_ATTENTE.equals(this.status)) {
            this.borrowDate = now;
            this.status = BorrowingStatus.EMPRUNTE;
            this.dueDate = now.plusDays(this.user.getUserType() == UserType.STAFF ? 30 : 14);
        }
    }

    public void setBorrowDate(LocalDateTime date) {
        this.borrowDate = date;
        if (this.dueDate == null && this.user != null) {
            this.dueDate = date.plusDays(this.user.getUserType() == UserType.STAFF ? 30 : 14);
        }
    }

    // Getters et Setters pour rating et ratingComment
    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getRatingComment() {
        return ratingComment;
    }

    public void setRatingComment(String ratingComment) {
        this.ratingComment = ratingComment;
    }

    // Méthode utilitaire pour vérifier si l'emprunt a une évaluation
    public boolean hasRating() {
        return rating != null;
    }
    
    // Getters and setters for notification tracking fields
    public boolean isOverdueNotified() {
        return overdueNotified;
    }
    
    public void setOverdueNotified(boolean overdueNotified) {
        this.overdueNotified = overdueNotified;
    }
    
    public boolean isDueSoonNotified() {
        return dueSoonNotified;
    }
    
    public void setDueSoonNotified(boolean dueSoonNotified) {
        this.dueSoonNotified = dueSoonNotified;
    }

    public void returnBook(String condition) {
        if (!BorrowingStatus.EMPRUNTE.equals(this.status)) {
            throw new IllegalStateException("Ce livre n'est pas actuellement emprunté");
        }
        this.returnDate = LocalDateTime.now();
        this.conditionAfter = condition;
        this.status = BorrowingStatus.RETOURNE;
        calculateFine();
    }

    @PreUpdate
    protected void onUpdate() {
        // Mise à jour du statut si retourné
        if (status == null) {
            status = BorrowingStatus.EN_ATTENTE;
        }
        if (returnDate != null && BorrowingStatus.EMPRUNTE.equals(status)) {
            status = BorrowingStatus.RETOURNE;
        }
        // Calcul des amendes si en retard
        if (isOverdue() && fineAmount.equals(BigDecimal.ZERO)) {
            calculateFine();
        }
        // Validation du renouvellement
        if (lastRenewalDate != null && renewalCount > getMaxRenewals()) {
            throw new IllegalStateException("Nombre maximum de renouvellements dépassé");
        }
    }


    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = BorrowingStatus.EN_ATTENTE;
        }
        if (fineAmount == null) {
            fineAmount = BigDecimal.ZERO;
        }
    }

    // Méthodes de calcul des amendes
    private void calculateFine() {
        if (isOverdue()) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, LocalDateTime.now());
            this.fineAmount = new BigDecimal("0.50").multiply(BigDecimal.valueOf(daysLate));
        }
    }

    // Méthodes utilitaires
    public void markAsLost() {
        this.status = BorrowingStatus.PERDU;
        if (book != null) {
            this.fineAmount = book.getReplacementCost();
        }
    }

    public void markAsDamaged(String condition) {
        this.conditionAfter = condition;
        if (!"BON".equals(condition) && book != null) {
            this.fineAmount = book.getReplacementCost().multiply(new BigDecimal("0.5"));
        }
    }
}

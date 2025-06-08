package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.library.enums.UserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Data
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
    private String status = "EN_ATTENTE";

    @Column(precision = 10, scale = 2)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    private String conditionBefore;
    private String conditionAfter;

    private int renewalCount = 0;
    private LocalDateTime lastRenewalDate;

    // Constructeur par défaut avec initialisation de la date actuelle
    public Borrowing() {
        this.borrowDate = LocalDateTime.now();
        this.fineAmount = BigDecimal.ZERO;
        this.status = "EN_ATTENTE";
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
        if (user != null && !user.getBorrowings().contains(this)) {
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
        return "EMPRUNTE".equals(status) 
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
        return "EMPRUNTE".equals(status) 
            && LocalDateTime.now().isAfter(dueDate);
    }

    private int getMaxRenewals() {
        return user.getUserType() == UserType.STAFF ? 2 : 1;
    }

    // Méthodes de gestion des dates
    public void setApprovalDate(LocalDateTime now) {
        if ("EN_ATTENTE".equals(this.status)) {
            this.borrowDate = now;
            this.status = "EMPRUNTE";
            this.dueDate = now.plusDays(this.user.getUserType() == UserType.STAFF ? 30 : 14);
        }
    }

    public void setBorrowDate(LocalDateTime date) {
        this.borrowDate = date;
        if (this.dueDate == null) {
            this.dueDate = date.plusDays(this.user.getUserType() == UserType.STAFF ? 30 : 14);
        }
    }

    // Ajout des nouveaux champs pour les évaluations
    @Column(name = "rating")
    private Integer rating;
     @Column(name = "rating_comment", length = 500)
    private String ratingComment;

    // Getters et Setters existants...

    // Nouveaux getters et setters pour rating et ratingComment
    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
    @Column(length = 500)
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

    public void returnBook(String condition) {
        if (!"EMPRUNTE".equals(this.status)) {
            throw new IllegalStateException("Ce livre n'est pas actuellement emprunté");
        }
        this.returnDate = LocalDateTime.now();
        this.conditionAfter = condition;
        this.status = "RETOURNE";
        calculateFine();
    }

    // Une seule méthode @PreUpdate
    @PreUpdate
    protected void onUpdate() {
        // Mise à jour du statut si retourné
        if (returnDate != null && "EMPRUNTE".equals(status)) {
            status = "RETOURNE";
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
        if (borrowDate == null) {
            borrowDate = LocalDateTime.now();
        }
        if (dueDate == null && user != null) {
            dueDate = borrowDate.plusDays(user.getUserType() == UserType.STAFF ? 30 : 14);
        }
        if (status == null) {
            status = "EN_ATTENTE";
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
        this.status = "PERDU";
        this.fineAmount = book.getReplacementCost();
    }

    public void markAsDamaged(String condition) {
        this.conditionAfter = condition;
        if (!"BON".equals(condition)) {
            this.fineAmount = book.getReplacementCost().multiply(new BigDecimal("0.5"));
        }
    }
}
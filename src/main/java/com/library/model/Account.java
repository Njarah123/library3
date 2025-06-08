package com.library.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "accounts")
public class Account {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
@JoinColumn(name = "user_id")
private User user;
    @Column(name = "no_borrowed_books", nullable = false)
    private int noBorrowedBooks = 0;
    
    @Column(name = "no_lost_books", nullable = false)
    private int noLostBooks = 0;
    
    @Column(name = "no_returned_books", nullable = false)
    private int noReturnedBooks = 0;
    
    @Column(name = "no_reserved_books", nullable = false)
    private int noReservedBooks = 0;
    
    @Column(name = "fine_amount", nullable = false)
    private BigDecimal fineAmount = BigDecimal.ZERO;

    // Constructeur par défaut
    public Account() {
        this.noBorrowedBooks = 0;
        this.noLostBooks = 0;
        this.noReturnedBooks = 0;
        this.noReservedBooks = 0;
        this.fineAmount = BigDecimal.ZERO;
    }

    // Getters and Setters avec valeurs par défaut
    public int getNoBorrowedBooks() {
        return noBorrowedBooks;
    }

    public void setNoBorrowedBooks(int noBorrowedBooks) {
        this.noBorrowedBooks = noBorrowedBooks;
    }

    public int getNoLostBooks() {
        return noLostBooks;
    }

    public void setNoLostBooks(int noLostBooks) {
        this.noLostBooks = noLostBooks;
    }

    public int getNoReturnedBooks() {
        return noReturnedBooks;
    }

    public void setNoReturnedBooks(int noReturnedBooks) {
        this.noReturnedBooks = noReturnedBooks;
    }

    public int getNoReservedBooks() {
        return noReservedBooks;
    }

    public void setNoReservedBooks(int noReservedBooks) {
        this.noReservedBooks = noReservedBooks;
    }

    public BigDecimal getFineAmount() {
        return fineAmount != null ? fineAmount : BigDecimal.ZERO;
    }

    public void setFineAmount(BigDecimal fineAmount) {
        this.fineAmount = fineAmount != null ? fineAmount : BigDecimal.ZERO;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public double calculateFine() {
        return getFineAmount().doubleValue();
    }
}
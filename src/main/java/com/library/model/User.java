package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.library.enums.UserType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(exclude = {"account", "borrowings", "reservations", "notifications"})
@ToString(exclude = {"account", "borrowings", "reservations", "notifications"})
@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id",
    scope = User.class)
public abstract class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    @JsonIgnore
    private String password;
    
    @Column(unique = true)
    private String email;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserType userType;
    private boolean active = true;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "balance")
    private Double balance = 0.0;
    
    // Dans la classe User, modifiez cette partie :
@OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
@JsonIgnoreProperties("user")
private Account account;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Borrowing> borrowings = new ArrayList<>();
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Reservation> reservations = new ArrayList<>();
    
    // Add notifications field with proper annotations for circular reference handling
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonIgnoreProperties({"user", "hibernateLazyInitializer", "handler"})
    @JsonIgnore // Use DTOs instead of direct serialization to avoid circular references
    private List<Notification> notifications = new ArrayList<>();
    
    // Méthodes abstraites
    public abstract boolean verify();
    private String profileImagePath;

    // Méthodes communes
    public boolean checkAccount() {
        return account != null && account.calculateFine() <= 0;
    }
    
    public Book getBookInfo(Long bookId) {
        // Implémentation pour obtenir les informations d'un livre
        return null; // À implémenter avec le repository
    }


 public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    
 public String getProfileImagePath() {
        return profileImagePath;
    }

    public void setProfileImagePath(String profileImagePath) {
        this.profileImagePath = profileImagePath;
    }

    // Méthodes pour la gestion des emprunts
    public boolean canBorrowBooks() {
        if (account == null) {
            return false;
        }
        return account.getNoBorrowedBooks() < getMaxBorrowingLimit() &&
               (account.getFineAmount() == null || account.getFineAmount().compareTo(BigDecimal.ZERO) <= 0);
    }
    
    public int getMaxBorrowingLimit() {
        return userType == UserType.STAFF ? 10 : 5;
    }
    
    public int getMaxReservationLimit() {
        return userType == UserType.STAFF ? 5 : 3;
    }
    
    // Méthodes pour la sécurité
    public String getRole() {
        return userType.getRole();
    }
    
    public boolean isStaff() {
        return userType == UserType.STAFF;
    }
    
    public boolean isStudent() {
        return userType == UserType.STUDENT;
    }

    // Getter et Setter pour email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    // Getter and Setter for balance
    public Double getBalance() {
        return balance;
    }
    
    public void setBalance(Double balance) {
        this.balance = balance != null ? balance : 0.0;
    }
}
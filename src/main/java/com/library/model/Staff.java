package com.library.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staff")
@PrimaryKeyJoinColumn(name = "user_id")
public class Staff extends User {
    
    @Column(nullable = false)
    private String dept;
    
    @Column(unique = true)
    private String employeeId;
    
    @Override
    public boolean verify() {
        // Implémentation spécifique pour la vérification du staff
        return this.getEmployeeId() != null && !this.getEmployeeId().isEmpty();
    }
    
    // Méthodes spécifiques au staff
    public boolean manageBooks(Book book) {
        // Logique pour gérer les livres
        return true;
    }
    
    public boolean manageUsers(User user) {
        // Logique pour gérer les utilisateurs
        return true;
    }
    
    public boolean generateReports() {
        // Logique pour générer des rapports
        return true;
    }
}
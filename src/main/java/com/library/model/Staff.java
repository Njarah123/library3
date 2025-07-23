package com.library.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "staff")
@PrimaryKeyJoinColumn(name = "user_id")
public class Staff extends User {
    
    @Column(name = "dept", nullable = false)
    private String dept;
    
    @Column(name = "employee_id", unique = true)
    private String employeeId;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructeurs
    public Staff() {
        super();
    }

    // Méthode appelée avant la persistance
    @PrePersist
    protected void onCreate() {
        updatedAt = LocalDateTime.now();
    }

    // Méthode appelée avant la mise à jour
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @Override
    public boolean verify() {
        return this.getEmployeeId() != null && 
               !this.getEmployeeId().isEmpty() && 
               this.getDept() != null && 
               !this.getDept().isEmpty();
    }
    
    // Méthodes de gestion des livres
    public boolean manageBooks(Book book) {
        if (book == null) {
            return false;
        }
        // Logique pour gérer les livres
        return true;
    }
    
    public boolean manageUsers(User user) {
        if (user == null) {
            return false;
        }
        // Logique pour gérer les utilisateurs
        return true;
    }
    
    public boolean generateReports() {
        try {
            // Logique pour générer des rapports
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Getters et Setters spécifiques si nécessaire
    public String getDept() {
        return dept;
    }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getCreatedAt() {
        return super.getCreatedAt();
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Builder pattern pour faciliter la création d'instances
    public static StaffBuilder builder() {
        return new StaffBuilder();
    }

    public static class StaffBuilder {
        private Staff staff;

        public StaffBuilder() {
            staff = new Staff();
        }

        public StaffBuilder dept(String dept) {
            staff.setDept(dept);
            return this;
        }

        public StaffBuilder employeeId(String employeeId) {
            staff.setEmployeeId(employeeId);
            return this;
        }

        public StaffBuilder username(String username) {
            staff.setUsername(username);
            return this;
        }

        public StaffBuilder password(String password) {
            staff.setPassword(password);
            return this;
        }

        public StaffBuilder email(String email) {
            staff.setEmail(email);
            return this;
        }

        public Staff build() {
            if (!staff.verify()) {
                throw new IllegalStateException("Staff validation failed");
            }
            return staff;
        }
    }

    // Méthode toString() pour le débogage
    @Override
    public String toString() {
        return "Staff{" +
                "id=" + getId() +
                ", username='" + getUsername() + '\'' +
                ", email='" + getEmail() + '\'' +
                ", dept='" + dept + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
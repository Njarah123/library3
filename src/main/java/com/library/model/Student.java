package com.library.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "students")
@PrimaryKeyJoinColumn(name = "user_id")
public class Student extends User {
    
    @Column(unique = true, nullable = false)
    private String studentId;
    
    private String className;

    @Override
    public boolean verify() {
        // Implémentation spécifique pour la vérification de l'étudiant
        return this.getStudentId() != null && !this.getStudentId().isEmpty();
    }
}
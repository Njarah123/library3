package com.library.model;

import java.time.LocalDateTime;

import com.library.enums.UserType;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "members")
public class Member extends User {
    
    public Member() {
        this.setUserType(UserType.STUDENT); // ou un autre type par défaut
        this.setCreatedAt(LocalDateTime.now());
        this.setActive(true);
    }

    @Override
    public boolean verify() {
        // Implémentation de la méthode abstraite
        return true; // ou votre logique de vérification
    }
}
package com.library.model;

import com.library.enums.UserType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "librarians")
@PrimaryKeyJoinColumn(name = "user_id")
public class Librarian extends User {
    
    @Column(unique = true, nullable = false)
    private String employeeId;
    
    @Column(unique = true)
    private String searchString;
    
    @OneToMany(mappedBy = "librarian", cascade = CascadeType.ALL)
    private List<Book> managedBooks = new ArrayList<>();

    @Override
    public boolean verify() {
        return verifyLibrarian();
    }
    
    public boolean verifyLibrarian() {
        return employeeId != null && !employeeId.isEmpty();
    }
    
    public void search(String query) {
        this.searchString = query;
        // Logique de recherche
    }

    // Méthodes de gestion des livres
    public Book addBook(Book book) {
        if (!verifyLibrarian()) {
            throw new RuntimeException("Unauthorized librarian access");
        }
        book.setLibrarian(this);
        managedBooks.add(book);
        return book;
    }

    public void deleteBook(Book book) {
        if (!verifyLibrarian()) {
            throw new RuntimeException("Unauthorized librarian access");
        }
        book.setLibrarian(null);
        managedBooks.remove(book);
    }

    public void updateBook(Book book) {
        if (!verifyLibrarian()) {
            throw new RuntimeException("Unauthorized librarian access");
        }
        // Logique de mise à jour
    }

    public void display() {
        // Afficher les informations du bibliothécaire
    }
}
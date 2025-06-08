package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.library.enums.UserType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "books")
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String publisher;

    @Column(name = "publication_year")
    private Integer publicationYear;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer availableQuantity;

    @Column(nullable = false)
    private Boolean available = true;

    @Column(name = "added_date")
    private LocalDateTime addedDate;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "shelf_location")
    private String shelfLocation;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "language")
    private String language;

    @Column(name = "edition")
    private String edition;

    

    @OneToMany(mappedBy = "book")
    private List<Borrowing> borrowings;
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();

    @Column(name = "total_borrows")
    private Integer totalBorrows = 0;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "number_of_ratings")
    private Integer numberOfRatings = 0;

    @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "librarian_id")
private User librarian;

    private BigDecimal purchasePrice;

    @Column(name = "replacement_cost", precision = 10, scale = 2)
private BigDecimal replacementCost = BigDecimal.ZERO;

    // Méthodes de gestion des emprunts
    public boolean canBeBorrowed() {
        return available && availableQuantity > 0;
    }


     public Borrowing getCurrentBorrowing() {
        if (borrowings != null) {
            return borrowings.stream()
                .filter(b -> "EN_COURS".equals(b.getStatus()))
                .findFirst()
                .orElse(null);
        }
        return null;
    }


     private String imagePath; // Chemin de l'image

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    
    public void decrementAvailableQuantity() {
        if (availableQuantity > 0) {
            availableQuantity--;
            if (availableQuantity == 0) {
                available = false;
            }
            this.lastUpdated = LocalDateTime.now();
        } else {
            throw new RuntimeException("Aucun exemplaire disponible");
        }
    }

    public void incrementAvailableQuantity() {
        if (availableQuantity < quantity) {
            availableQuantity++;
            available = true;
            this.lastUpdated = LocalDateTime.now();
        }
    }

    // Méthodes de gestion des réservations
    public boolean hasActiveReservations() {
        return reservations.stream()
                .anyMatch(r -> r.getStatus().equals("EN_ATTENTE"));
    }

    public int getActiveReservationsCount() {
        return (int) reservations.stream()
                .filter(r -> r.getStatus().equals("EN_ATTENTE"))
                .count();
    }

    // Méthodes de gestion des notes
    public void addRating(int newRating) {
        if (newRating < 1 || newRating > 5) {
            throw new IllegalArgumentException("La note doit être entre 1 et 5");
        }
        
        double totalRating = (rating != null ? rating * numberOfRatings : 0) + newRating;
        numberOfRatings++;
        rating = totalRating / numberOfRatings;
        this.lastUpdated = LocalDateTime.now();
    }

public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public Integer getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(Integer numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }



    // Méthodes de gestion du stock
    public boolean isLowStock() {
        return availableQuantity <= (quantity * 0.2); // 20% du stock total
    }

    public boolean needsRestock() {
        return availableQuantity <= (quantity * 0.1); // 10% du stock total
    }

    // Méthodes de statistiques
    public double getBorrowRate() {
        if (totalBorrows == 0) return 0.0;
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        long recentBorrows = borrowings.stream()
                .filter(b -> b.getBorrowDate().isAfter(oneMonthAgo))
                .count();
        return (double) recentBorrows / totalBorrows;
    }

      public void setLibrarian(User librarian) {
        if (librarian != null && librarian.getUserType() != UserType.LIBRARIAN) {
            throw new IllegalArgumentException("Only librarians can be assigned to books");
        }
        this.librarian = librarian;
    }


      public BigDecimal getReplacementCost() {
    // Si le coût de remplacement n'est pas défini, on utilise le prix d'achat majoré de 20%
    if (replacementCost.equals(BigDecimal.ZERO) && purchasePrice != null) {
        return purchasePrice.multiply(new BigDecimal("1.20"));
    }
    return replacementCost;
}

public void setReplacementCost(BigDecimal replacementCost) {
    if (replacementCost != null && replacementCost.compareTo(BigDecimal.ZERO) < 0) {
        throw new IllegalArgumentException("Le coût de remplacement ne peut pas être négatif");
    }
    this.replacementCost = replacementCost != null ? replacementCost : BigDecimal.ZERO;
}



    // Getters et Setters spécifiques
    public void setQuantity(Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }
        this.quantity = quantity;
        if (this.availableQuantity == null || this.availableQuantity > quantity) {
            this.availableQuantity = quantity;
        }
        this.lastUpdated = LocalDateTime.now();
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        if (availableQuantity < 0) {
            throw new IllegalArgumentException("La quantité disponible ne peut pas être négative");
        }
        if (quantity != null && availableQuantity > quantity) {
            throw new IllegalArgumentException("La quantité disponible ne peut pas être supérieure à la quantité totale");
        }
        this.availableQuantity = availableQuantity;
        this.available = availableQuantity > 0;
        this.lastUpdated = LocalDateTime.now();
    }

    // Constructeurs
    public Book() {
        this.addedDate = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.totalBorrows = 0;
        this.numberOfRatings = 0;
        this.rating = 0.0;
    }

    // Méthodes de validation
    public void validateIsbn() {
        if (isbn == null || isbn.trim().isEmpty()) {
            throw new IllegalArgumentException("L'ISBN ne peut pas être vide");
        }
        // Validation du format ISBN-13
        isbn = isbn.replaceAll("[^0-9]", "");
        if (isbn.length() != 13) {
            throw new IllegalArgumentException("L'ISBN doit contenir 13 chiffres");
        }
    }

    @PrePersist
    protected void onCreate() {
        addedDate = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (availableQuantity == null) {
            availableQuantity = quantity;
        }
        if (totalBorrows == null) {
            totalBorrows = 0;
        }
        if (numberOfRatings == null) {
            numberOfRatings = 0;
        }
        if (rating == null) {
            rating = 0.0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Méthode toString personnalisée
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", isbn='" + isbn + '\'' +
                ", available=" + available +
                ", quantity=" + quantity +
                ", availableQuantity=" + availableQuantity +
                '}';
    }
}
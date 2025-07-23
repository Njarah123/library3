package com.library.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BookDetailsDTO {
    private Long id;
    private String title;
    private String author;
    private String description;
    private String isbn;
    private String category;
    private String publisher;
    private Integer publicationYear;
    private String language;
    private String edition;
    private Integer quantity;
    private Integer availableQuantity;
    private String imagePath;
    private Double rating;
    private String status;
    private Boolean available;
}
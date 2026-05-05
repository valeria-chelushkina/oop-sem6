package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    private Long id;
    private String title;
    private String isbn;
    private String publisher;
    private Integer publicationYear;
    private String coverURL;
    private String language;
    private Integer pagesCount;
    private String description;
    // average rating from book_ratings; null if there are no ratings yet
    private Double averageRating;
    // number of ratings
    private Integer ratingsCount;
    // a number how many times a loan with this book_id was returned
    private Integer timesRead;
    // many-to-many relation with authors and genres
    private List<Author> authors;
    private List<Genre> genres;
}
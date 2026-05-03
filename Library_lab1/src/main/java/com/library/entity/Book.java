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
    // many-to-many relation with authors and genres
    private List<Author> authors;
    private List<Genre> genres;
}
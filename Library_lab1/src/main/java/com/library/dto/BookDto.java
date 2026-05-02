package com.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDto {
    private Long id;
    private String title;
    private String isbn;
    private String publisher;
    private Integer publicationYear;
    private String coverURL;
    private String language;
    private Integer pagesCount;
    private String genre;
    private String description;
    private List<AuthorDto> authors;
}

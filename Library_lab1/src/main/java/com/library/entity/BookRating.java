package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookRating {
    private Long id;
    private Long bookId;
    private Long userId;
    private int rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
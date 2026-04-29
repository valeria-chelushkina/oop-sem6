package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookItems {
    private Long id;
    private Long bookId;
    private String inventoryCode;
    private String status; // example - AVAILABLE, LOST, LOANED, RESERVED
}

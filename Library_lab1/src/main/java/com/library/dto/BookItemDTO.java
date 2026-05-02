package com.library.dto;

import com.library.entity.enums.BookItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookItemDTO {
    private Long id;
    private Long bookId;
    private String inventoryCode;
    private BookItemStatus status;
}

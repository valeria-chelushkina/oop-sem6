package com.library.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Loan {
    private Long id;
    private Long bookId;
    private Long readerId;
    private Long librarianId;
    private LocalDateTime loanDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private String loanType; //SUBSCRIPTION or READING_ROOM
    private String status; //ORDERED, ISSUED, RETURNED, OVERDUE
}

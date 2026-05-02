package com.library.dto;

import com.library.entity.enums.LoanStatus;
import com.library.entity.enums.LoanType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDto {
    private Long id;
    private Long bookItemId;
    private Long readerId;
    private Long librarianId;
    private LocalDateTime loanDate;
    private LocalDate dueDate;
    private LocalDateTime returnDate;
    private LoanType loanType;
    private LoanStatus status;
}

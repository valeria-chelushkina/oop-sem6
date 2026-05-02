package com.library.mapper;

import com.library.dto.CreateLoanRequest;
import com.library.dto.LoanDTO;
import com.library.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface LoanMapper {
    LoanDTO toDto(Loan loan);

    Loan toEntity(LoanDTO dto);

    @Mapping(target = "id", ignore = true)
    Loan toEntity(CreateLoanRequest request);
}

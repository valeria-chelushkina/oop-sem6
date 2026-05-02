package com.library.mapper;

import com.library.dto.CreateLoanRequest;
import com.library.dto.LoanDto;
import com.library.entity.Loan;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "default")
public interface LoanMapper {
    LoanDto toDto(Loan loan);

    Loan toEntity(LoanDto dto);

    @Mapping(target = "id", ignore = true)
    Loan toEntity(CreateLoanRequest request);
}

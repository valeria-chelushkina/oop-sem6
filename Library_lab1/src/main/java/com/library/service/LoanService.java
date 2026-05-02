package com.library.service;

import com.library.dto.LoanDTO;
import com.library.entity.enums.LoanType;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface LoanService {
    List<LoanDTO> findAll() throws SQLException;
    LoanDTO findById(Long id) throws SQLException;
    List<LoanDTO> findActiveLoans() throws SQLException;
    List<LoanDTO> findOrderedByReader(Long readerId) throws SQLException;
    int createOrder(Long bookItemId, Long readerId, LoanType loanType, Date dueDate) throws SQLException;
    Long create(LoanDTO loanDTO) throws SQLException;
    int issueLoan(Long loanId, Long librarianId) throws SQLException;
    int returnLoan(Long loanId) throws SQLException;
    int update(LoanDTO loanDTO) throws SQLException;
    int deleteById(Long id) throws SQLException;
}
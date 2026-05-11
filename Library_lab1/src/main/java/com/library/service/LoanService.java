package com.library.service;

import com.library.dto.LoanDTO;
import com.library.entity.enums.LoanType;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public interface LoanService {
    List<LoanDTO> findAll() throws SQLException;
    LoanDTO findById(Long id) throws SQLException;
    
    /**
     * Retrieves all loans that are either ISSUED or OVERDUE.
     */
    List<LoanDTO> findActiveLoans() throws SQLException;
    
    List<LoanDTO> findOrderedByReader(Long readerId) throws SQLException;
    
    /**
     * Checks if a reader has an active (ORDERED or ISSUED) loan for a specific book.
     * Used to prevent duplicate orders.
     */
    List<LoanDTO> findActiveByBookAndReader(Long bookId, Long readerId) throws SQLException;
    
    /**
     * Creates a new loan record with status 'ORDERED'.
     */
    int createOrder(Long bookItemId, Long readerId, LoanType loanType, Date dueDate) throws SQLException;
    
    Long create(LoanDTO loanDTO) throws SQLException;
    
    /**
     * Updates an ORDERED loan to ISSUED status and assigns a librarian.
     */
    int issueLoan(Long loanId, Long librarianId) throws SQLException;
    
    /**
     * Marks a loan as RETURNED (or other final states) and sets the return date.
     */
    int returnLoan(Long loanId) throws SQLException;
    
    int update(LoanDTO loanDTO) throws SQLException;
    int deleteById(Long id) throws SQLException;
}
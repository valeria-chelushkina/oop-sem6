package com.library.service;

import com.library.dao.LoanDAO;
import com.library.dto.LoanDTO;
import com.library.entity.Loan;
import com.library.entity.enums.LoanType;
import com.library.mapper.LoanMapper;
import org.mapstruct.factory.Mappers;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class LoanServiceImpl implements LoanService {
    private final LoanDAO loanDAO;
    private final LoanMapper loanMapper;

    public LoanServiceImpl() {
        this.loanDAO = new LoanDAO();
        this.loanMapper = Mappers.getMapper(LoanMapper.class);
    }

    @Override
    public List<LoanDTO> findAll() throws SQLException {
        return loanDAO.findAll().stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LoanDTO findById(Long id) throws SQLException {
        List<Loan> loans = loanDAO.findById(id);
        if (loans.isEmpty()) {
            return null;
        }
        return loanMapper.toDto(loans.get(0));
    }

    @Override
    public List<LoanDTO> findActiveLoans() throws SQLException {
        return loanDAO.findActiveLoans().stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LoanDTO> findOrderedByReader(Long readerId) throws SQLException {
        return loanDAO.findOrderByReader(readerId).stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public int createOrder(Long bookItemId, Long readerId, LoanType loanType, Date dueDate) throws SQLException {
        return loanDAO.createOrder(bookItemId, readerId, loanType, dueDate);
    }

    @Override
    public Long create(LoanDTO loanDTO) throws SQLException {
        return loanDAO.create(loanMapper.toEntity(loanDTO));
    }

    @Override
    public int issueLoan(Long loanId, Long librarianId) throws SQLException {
        return loanDAO.issueLoan(loanId, librarianId);
    }

    @Override
    public int returnLoan(Long loanId) throws SQLException {
        return loanDAO.returnLoan(loanId);
    }

    @Override
    public int update(LoanDTO loanDTO) throws SQLException {
        if (loanDTO == null || loanDTO.getId() == null) {
            throw new IllegalArgumentException("Loan id is required for update.");
        }
        return loanDAO.update(loanMapper.toEntity(loanDTO));
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        return loanDAO.deleteById(id);
    }
}


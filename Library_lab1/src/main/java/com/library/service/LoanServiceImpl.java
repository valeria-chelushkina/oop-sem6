package com.library.service;

import com.library.dao.BookItemDAO;
import com.library.dao.LoanDAO;
import com.library.dto.LoanDTO;
import com.library.entity.BookItem;
import com.library.entity.Loan;
import com.library.entity.enums.BookItemStatus;
import com.library.entity.enums.LoanStatus;
import com.library.entity.enums.LoanType;
import com.library.mapper.LoanMapper;
import org.mapstruct.factory.Mappers;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class LoanServiceImpl implements LoanService {
    private final LoanDAO loanDAO;
    private final BookItemDAO bookItemDAO;
    private final LoanMapper loanMapper;

    public LoanServiceImpl() {
        this.loanDAO = new LoanDAO();
        this.bookItemDAO = new BookItemDAO();
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
    public List<LoanDTO> findActiveByBookAndReader(Long bookId, Long readerId) throws SQLException {
        return loanDAO.findActiveByBookAndReader(bookId, readerId).stream()
                .map(loanMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public int createOrder(Long bookItemId, Long readerId, LoanType loanType, Date dueDate) throws SQLException {
        int affected = loanDAO.createOrder(bookItemId, readerId, loanType, dueDate);
        if (affected > 0) {
            updateBookItemStatus(bookItemId, BookItemStatus.ORDERED);
        }
        return affected;
    }

    @Override
    public Long create(LoanDTO loanDTO) throws SQLException {
        Long id = loanDAO.create(loanMapper.toEntity(loanDTO));
        if (id != null && loanDTO.getStatus() != null) {
            updateBookItemStatus(loanDTO.getBookItemId(), BookItemStatus.valueOf(loanDTO.getStatus().name()));
        }
        return id;
    }

    @Override
    public int issueLoan(Long loanId, Long librarianId) throws SQLException {
        List<Loan> loans = loanDAO.findById(loanId);
        if (loans.isEmpty()) return 0;
        Loan loan = loans.get(0);

        int affected = loanDAO.issueLoan(loanId, librarianId);
        if (affected > 0) {
            updateBookItemStatus(loan.getBookItemId(), BookItemStatus.ISSUED);
        }
        return affected;
    }

    @Override
    public int returnLoan(Long loanId) throws SQLException {
        List<Loan> loans = loanDAO.findById(loanId);
        if (loans.isEmpty()) return 0;
        Loan loan = loans.get(0);

        int affected = loanDAO.returnLoan(loanId);
        if (affected > 0) {
            BookItemStatus targetStatus = (loan.getLoanType() == LoanType.READING_ROOM) ? 
                    BookItemStatus.READING_ROOM_ONLY : BookItemStatus.AVAILABLE;
            updateBookItemStatus(loan.getBookItemId(), targetStatus);
        }
        return affected;
    }

    @Override
    public int update(LoanDTO loanDTO) throws SQLException {
        if (loanDTO == null || loanDTO.getId() == null) {
            throw new IllegalArgumentException("Loan id is required for update.");
        }
        int affected = loanDAO.update(loanMapper.toEntity(loanDTO));
        if (affected > 0 && loanDTO.getStatus() != null) {
            BookItemStatus biStatus;
            switch (loanDTO.getStatus()) {
                case ORDERED: biStatus = BookItemStatus.ORDERED; break;
                case ISSUED: biStatus = BookItemStatus.ISSUED; break;
                case RETURNED: 
                    biStatus = (loanDTO.getLoanType() == LoanType.READING_ROOM) ? 
                            BookItemStatus.READING_ROOM_ONLY : BookItemStatus.AVAILABLE;
                    break;
                case LOST: biStatus = BookItemStatus.LOST; break;
                case DAMAGED: biStatus = BookItemStatus.DAMAGED; break;
                case ARCHIVED: biStatus = BookItemStatus.ARCHIVED; break;
                default: return affected;
            }
            updateBookItemStatus(loanDTO.getBookItemId(), biStatus);
        }
        return affected;
    }

    @Override
    public int deleteById(Long id) throws SQLException {
        List<Loan> loans = loanDAO.findById(id);
        if (!loans.isEmpty()) {
            Loan loan = loans.get(0);
            if (loan.getStatus() == LoanStatus.ORDERED) {
                BookItemStatus targetStatus = (loan.getLoanType() == LoanType.READING_ROOM) ? 
                        BookItemStatus.READING_ROOM_ONLY : BookItemStatus.AVAILABLE;
                updateBookItemStatus(loan.getBookItemId(), targetStatus);
            }
        }
        return loanDAO.deleteById(id);
    }

    private void updateBookItemStatus(Long bookItemId, BookItemStatus status) throws SQLException {
        List<BookItem> items = bookItemDAO.findById(bookItemId);
        if (!items.isEmpty()) {
            BookItem item = items.get(0);
            item.setStatus(status);
            bookItemDAO.update(item);
        }
    }
}


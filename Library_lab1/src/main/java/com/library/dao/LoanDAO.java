package com.library.dao;

import com.library.entity.Loan;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LoanDAO extends BaseDAO {
    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        Timestamp loanDateTs = rs.getTimestamp("loan_date");
        Timestamp returnDateTs = rs.getTimestamp("return_date");
        Date dueDateSql = rs.getDate("due_date");
        return Loan.builder()
                .id(rs.getLong("id"))
                .bookItemId(rs.getLong("book_item_id"))
                .readerId(rs.getLong("reader_id"))
                .librarianId(rs.getLong("librarian_id"))
                .loanDate(loanDateTs != null ? loanDateTs.toLocalDateTime() : null)
                .dueDate(dueDateSql != null ? dueDateSql.toLocalDate() : null)
                .returnDate(returnDateTs != null ? returnDateTs.toLocalDateTime() : null)
                .loanType(rs.getString("loan_type"))
                .status(rs.getString("status"))
                .build();
    }

    public List<Loan> findAll() throws SQLException {
        String sql = "SELECT * FROM loans";
        String loggerMessage = "Fetching all loans from the catalogue.";
        return query(sql, Collections.emptyList(), this::mapResultSetToLoan, loggerMessage);
    }

    public int createOrder(Long bookItemId, Long readerId, String loanType, Date dueDate) throws SQLException {
        String sql = "INSERT INTO loans (book_item_id, reader_id, loan_date, due_date, loan_type, status) " +
                "VALUES (?, ?, NOW(), ?, ?, 'ORDERED')";
        String loggerMessage = "Creating new ORDERED loan.";
        return update(sql, Arrays.asList(bookItemId, readerId, dueDate, loanType), loggerMessage);
    }

    public int issueLoan(Long id, Long librarianId) throws SQLException {
        String sql = "UPDATE loans SET status = 'ISSUED', librarian_id = ? WHERE id = ?";
        String loggerMessage = "Updating loan to ISSUED.";
        return update(sql, List.of(librarianId, id), loggerMessage);
    }

    public int returnLoan(Long id) throws SQLException {
        String sql = "UPDATE loans SET status = 'RETURNED', return_date = NOW() WHERE id = ?";
        String loggerMessage = "Updating loan to RETURNED.";
        return update(sql, List.of(id), loggerMessage);
    }

    public List<Loan> findOrderByReader(Long id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE reader_id = ? AND status = 'ORDERED'";
        String loggerMessage = "Fetching ORDERED loans by reader.";
        return query(sql, List.of(id), this::mapResultSetToLoan, loggerMessage);
    }

    public List<Loan> findActiveLoans() throws SQLException {
        String sql = "SELECT * FROM loans WHERE status IN ('ISSUED', 'ORDERED')";
        String loggerMessage = "Fetching all active loans.";
        return query(sql, Collections.emptyList(), this::mapResultSetToLoan, loggerMessage);
    }

    public Long create(Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (book_item_id, reader_id, librarian_id, loan_date, due_date, return_date, loan_type, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String loggerMessage = "Creating new loan.";
        Timestamp loanDate = loan.getLoanDate() != null ? Timestamp.valueOf(loan.getLoanDate()) : null;
        Timestamp returnDate = loan.getReturnDate() != null ? Timestamp.valueOf(loan.getReturnDate()) : null;
        Date dueDate = loan.getDueDate() != null ? Date.valueOf(loan.getDueDate()) : null;
        return insertAndReturnId(
                sql,
                Arrays.asList(
                        loan.getBookItemId(),
                        loan.getReaderId(),
                        loan.getLibrarianId(),
                        loanDate,
                        dueDate,
                        returnDate,
                        loan.getLoanType(),
                        loan.getStatus()
                ),
                loggerMessage
        );
    }

    public int update(Loan loan) throws SQLException {
        String sql = "UPDATE loans SET book_item_id = ?, reader_id = ?, librarian_id = ?, loan_date = ?, due_date = ?, return_date = ?, loan_type = ?, status = ? " +
                "WHERE id = ?";
        String loggerMessage = "Updating loan by id.";
        Timestamp loanDate = loan.getLoanDate() != null ? Timestamp.valueOf(loan.getLoanDate()) : null;
        Timestamp returnDate = loan.getReturnDate() != null ? Timestamp.valueOf(loan.getReturnDate()) : null;
        Date dueDate = loan.getDueDate() != null ? Date.valueOf(loan.getDueDate()) : null;
        return update(
                sql,
                Arrays.asList(
                        loan.getBookItemId(),
                        loan.getReaderId(),
                        loan.getLibrarianId(),
                        loanDate,
                        dueDate,
                        returnDate,
                        loan.getLoanType(),
                        loan.getStatus(),
                        loan.getId()
                ),
                loggerMessage
        );
    }

    public List<Loan> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM loans WHERE id = ?";
        String loggerMessage = "Fetching loans by id.";
        return query(sql, List.of(id), this::mapResultSetToLoan, loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM loans WHERE id = ?";
        String loggerMessage = "Deleting loan by id.";
        return update(sql, List.of(id), loggerMessage);
    }
}

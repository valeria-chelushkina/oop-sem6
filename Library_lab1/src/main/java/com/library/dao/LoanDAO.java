package com.library.dao;

import com.library.entity.Loan;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class LoanDAO extends BaseDAO {
    private Loan mapResultSetToLoan(ResultSet rs) throws SQLException {
        return Loan.builder()
                .id(rs.getLong("id"))
                .bookItemId(rs.getLong("book_item_id"))
                .readerId(rs.getLong("reader_id"))
                .librarianId(rs.getLong("librarian_id"))
                .loanDate(rs.getTimestamp("loan_date").toLocalDateTime())
                .dueDate(rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null)
                .returnDate(rs.getTimestamp("return_date") != null ?
                        rs.getTimestamp("return_date").toLocalDateTime() : null)
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
        String loggerMessage = "Creating new ORDERED loan";
        return update(sql, List.of(bookItemId, readerId, dueDate, loanType), loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM loans WHERE id = ?";
        String loggerMessage = "Deleting loan by id";
        return update(sql, List.of(id), loggerMessage);
    }
}

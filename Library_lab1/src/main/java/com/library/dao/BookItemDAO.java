package com.library.dao;

import com.library.entity.BookItem;
import com.library.entity.enums.BookItemStatus;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BookItemDAO extends BaseDAO {
    private BookItem mapResultSetToBookItem(ResultSet rs) throws SQLException {
        return BookItem.builder()
                .id(rs.getLong("id"))
                .bookId(rs.getLong("book_id"))
                .inventoryCode(rs.getString("inventory_code"))
                .status(BookItemStatus.valueOf(rs.getString("status")))
                .build();
    }

    public List<BookItem> findAll() throws SQLException {
        String sql = "SELECT * FROM book_items";
        String loggerMessage = "Fetching all book items from the catalogue.";
        return query(sql, Collections.emptyList(), this::mapResultSetToBookItem, loggerMessage);
    }

    public int countAvailableBookItems() throws SQLException {
        String sql = "SELECT COUNT(*) FROM book_items WHERE UPPER(status) = 'AVAILABLE'";
        return queryForInt(sql, Collections.emptyList(), "Counting all available book items from the catalogue.");
    }

    public Long create(BookItem bookItem) throws SQLException {
        String sql = "INSERT INTO book_items (book_id, inventory_code, status) VALUES (?, ?, ?)";
        String loggerMessage = "Creating new book item.";
        return insertAndReturnId(
                sql,
                Arrays.asList(bookItem.getBookId(), bookItem.getInventoryCode(), bookItem.getStatus() != null ? bookItem.getStatus().name() : null),
                loggerMessage
        );
    }

    public int update(BookItem bookItem) throws SQLException {
        String sql = "UPDATE book_items SET book_id = ?, inventory_code = ?, status = ? WHERE id = ?";
        String loggerMessage = "Updating book item by id.";
        return update(
                sql,
                Arrays.asList(
                        bookItem.getBookId(),
                        bookItem.getInventoryCode(),
                        bookItem.getStatus() != null ? bookItem.getStatus().name() : null,
                        bookItem.getId()
                ),
                loggerMessage
        );
    }

    public List<BookItem> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM book_items WHERE id = ?";
        String loggerMessage = "Fetching book item by id.";
        return query(sql, List.of(id), this::mapResultSetToBookItem, loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM book_items WHERE id = ?";
        String loggerMessage = "Deleting book items by id.";
        return update(sql, List.of(id), loggerMessage);
    }

}
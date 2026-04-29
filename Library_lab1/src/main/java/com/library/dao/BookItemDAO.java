package com.library.dao;

import com.library.entity.BookItem;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

public class BookItemDAO extends BaseDAO {
    private BookItem mapResultSetToBookItem(ResultSet rs) throws SQLException {
        return BookItem.builder()
                .id(rs.getLong("id"))
                .bookId(rs.getLong("book_id"))
                .inventoryCode(rs.getString("inventory_code"))
                .status(rs.getString("status"))
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

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM book_items WHERE id = ?";
        String loggerMessage = "Deleting book items by id";
        return update(sql, List.of(id), loggerMessage);
    }

}
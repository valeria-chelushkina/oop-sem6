package com.library.dao;

import com.library.entity.BookRating;
import com.library.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public class BookRatingDAO extends BaseDAO {

    private BookRating mapResultSetToBookRating(ResultSet rs) throws SQLException {
        Timestamp createdAtTs = rs.getTimestamp("created_at");
        Timestamp updatedAtTs = rs.getTimestamp("updated_at");
        return BookRating.builder()
                .id(rs.getLong("id"))
                .bookId(rs.getLong("book_id"))
                .userId(rs.getLong("user_id"))
                .rating(rs.getInt("rating"))
                .createdAt(createdAtTs != null ? createdAtTs.toLocalDateTime() : null)
                .updatedAt(updatedAtTs != null ? updatedAtTs.toLocalDateTime() : null)
                .build();
    }

    // creates or updates a rating (UNIQUE book_id + user_id).
    public BookRating upsert(BookRating bookRating) throws SQLException {
        String sql = "INSERT INTO book_ratings (book_id, user_id, rating, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?) "
                + "ON CONFLICT (book_id, user_id) DO UPDATE SET "
                + "rating = EXCLUDED.rating, "
                + "updated_at = EXCLUDED.updated_at "
                + "RETURNING id, book_id, user_id, rating, created_at, updated_at";
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime createdAt = bookRating.getCreatedAt() != null ? bookRating.getCreatedAt() : now;
        LocalDateTime updatedAt = bookRating.getUpdatedAt() != null ? bookRating.getUpdatedAt() : now;
        logger.info("Upsert rating book_id={} user_id={}", bookRating.getBookId(), bookRating.getUserId());
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, bookRating.getBookId());
            stmt.setLong(2, bookRating.getUserId());
            stmt.setInt(3, bookRating.getRating());
            stmt.setTimestamp(4, Timestamp.valueOf(createdAt));
            stmt.setTimestamp(5, Timestamp.valueOf(updatedAt));
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new SQLException("Upsert rating returned no row.");
                }
                BookRating row = mapResultSetToBookRating(rs);
                logger.info("Rating saved id={}", row.getId());
                return row;
            }
        } catch (SQLException e) {
            logger.error("Upsert rating failed", e);
            throw e;
        }
    }

    public List<BookRating> getByBookId(Long bookId) throws SQLException {
        String sql = "SELECT * FROM book_ratings WHERE book_id = ? ORDER BY id";
        String loggerMessage = "Fetching ratings by book id.";
        return query(sql, List.of(bookId), this::mapResultSetToBookRating, loggerMessage);
    }

    public int deleteByBookAndUser(Long bookId, Long userId) throws SQLException {
        String sql = "DELETE FROM book_ratings WHERE book_id = ? AND user_id = ?";
        String loggerMessage = "Deleting rating by book and user.";
        return update(sql, List.of(bookId, userId), loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM book_ratings WHERE id = ?";
        String loggerMessage = "Deleting rating by id.";
        return update(sql, List.of(id), loggerMessage);
    }
}

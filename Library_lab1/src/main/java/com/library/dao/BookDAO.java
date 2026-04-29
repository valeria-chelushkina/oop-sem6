package com.library.dao;

import com.library.entity.Book;
import com.library.util.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookDAO {
    private static final Logger logger = LogManager.getLogger(BookDAO.class);

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        return Book.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .isbn(rs.getString("isbn"))
                .publisher(rs.getString("publisher"))
                .publicationYear(rs.getInt("publication_year"))
                .coverURL(rs.getString("cover_url"))
                .language(rs.getString("language"))
                .pagesCount(rs.getInt("pages_count"))
                .genre(rs.getString("genre"))
                .description(rs.getString("description"))
                .build();
    }

    private List<Book> findByCondition(String sql, String loggerCondition, List<String> params) throws SQLException {
        List<Book> books = new ArrayList<>();
        logger.info(loggerCondition);
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapResultSetToBook(rs));
                }
            }
            logger.info("Fetched {} book records", books.size());
        } catch (SQLException e) {
            logger.error("Failed to fetch books from database", e);
            throw e;
        }
        return books;
    }

    public List<Book> findAll() throws SQLException {
        String sql = "SELECT * FROM books";
        String loggerCondition = "Fetching all books from the catalogue.";
        return findByCondition(sql, loggerCondition, Collections.emptyList());
    }

    public List<Book> findByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?)";
        String loggerCondition = "Fetching books by title";
        return findByCondition(sql, loggerCondition, List.of("%" + title + "%"));
    }

    public List<Book> findByAuthor(String author) throws SQLException {
        String sql = "SELECT DISTINCT b.* FROM books b " +
                "LEFT JOIN book_authors ba ON b.id = ba.book_id " +
                "LEFT JOIN authors a ON ba.author_id = a.id " +
                "WHERE LOWER(a.pen_name) LIKE LOWER(?)";
        String loggerCondition = "Fetching books by author";
        return findByCondition(sql, loggerCondition, List.of("%" + author + "%"));
    }

    public List<Book> findByGenre(String genre) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(genre) = LOWER(?)";
        String loggerCondition = "Fetching books by genre";
        return findByCondition(sql, loggerCondition, List.of(genre));
    }
}

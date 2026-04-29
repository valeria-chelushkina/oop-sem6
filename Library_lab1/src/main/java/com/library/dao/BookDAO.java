package com.library.dao;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.util.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookDAO extends BaseDAO {
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

    public List<Book> findAll() throws SQLException {
        String sql = "SELECT * FROM books";
        String loggerMessage = "Fetching all books from the catalogue.";
        return query(sql, Collections.emptyList(), this::mapResultSetToBook, loggerMessage);
    }

    public List<Book> findByTitle(String title) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(title) LIKE LOWER(?)";
        String loggerMessage = "Fetching books by title.";
        return query(sql, List.of("%" + title + "%"), this::mapResultSetToBook, loggerMessage);
    }

    public List<Book> findByAuthor(String author) throws SQLException {
        String sql = "SELECT DISTINCT b.* FROM books b " +
                "LEFT JOIN book_authors ba ON b.id = ba.book_id " +
                "LEFT JOIN authors a ON ba.author_id = a.id " +
                "WHERE LOWER(a.pen_name) LIKE LOWER(?)";
        String loggerMessage = "Fetching books by author.";
        return query(sql, List.of("%" + author + "%"), this::mapResultSetToBook, loggerMessage);
    }

    public List<Book> findByGenre(String genre) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(genre) = LOWER(?)";
        String loggerMessage = "Fetching books by genre.";
        return query(sql, List.of(genre), this::mapResultSetToBook, loggerMessage);
    }

    public Long create(Book book) throws SQLException {
        logger.info("Creating new book with title='{}'", book.getTitle());
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long createdBookId = insertBook(conn, book);
                linkBookAuthors(conn, createdBookId, book.getAuthors(), false);

                conn.commit();
                logger.info("Book created successfully with id={}", createdBookId);
                return createdBookId;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Failed to create book and book-author links", e);
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Long createWithAuthors(Book book) throws SQLException {
        logger.info("Creating new book with author upsert, title='{}'", book.getTitle());
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long createdBookId = insertBook(conn, book);
                linkBookAuthors(conn, createdBookId, book.getAuthors(), true);

                conn.commit();
                logger.info("Book with authors created successfully with id={}", createdBookId);
                return createdBookId;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Failed to create book with authors", e);
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public int update(Book book) throws SQLException {
        String updateBookSql = "UPDATE books SET title = ?, isbn = ?, publisher = ?, publication_year = ?, cover_url = ?, language = ?, pages_count = ?, genre = ?, description = ? " +
                "WHERE id = ?";
        String deleteLinksSql = "DELETE FROM book_authors WHERE book_id = ?";

        logger.info("Updating book with id={}", book.getId());
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int affectedRows;
                try (PreparedStatement stmt = conn.prepareStatement(updateBookSql)) {
                    stmt.setString(1, book.getTitle());
                    stmt.setString(2, book.getIsbn());
                    stmt.setString(3, book.getPublisher());
                    stmt.setObject(4, book.getPublicationYear());
                    stmt.setString(5, book.getCoverURL());
                    stmt.setString(6, book.getLanguage());
                    stmt.setObject(7, book.getPagesCount());
                    stmt.setString(8, book.getGenre());
                    stmt.setString(9, book.getDescription());
                    stmt.setLong(10, book.getId());
                    affectedRows = stmt.executeUpdate();
                }

                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteLinksSql)) {
                    deleteStmt.setLong(1, book.getId());
                    deleteStmt.executeUpdate();
                }

                linkBookAuthors(conn, book.getId(), book.getAuthors(), true);

                conn.commit();
                logger.info("Book updated successfully, affected rows={}", affectedRows);
                return affectedRows;
            } catch (SQLException e) {
                conn.rollback();
                logger.error("Failed to update book and its authors", e);
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    private Long insertBook(Connection conn, Book book) throws SQLException {
        String insertBookSql = "INSERT INTO books (title, isbn, publisher, publication_year, cover_url, language, pages_count, genre, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement bookStmt = conn.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
            bookStmt.setString(1, book.getTitle());
            bookStmt.setString(2, book.getIsbn());
            bookStmt.setString(3, book.getPublisher());
            bookStmt.setObject(4, book.getPublicationYear());
            bookStmt.setString(5, book.getCoverURL());
            bookStmt.setString(6, book.getLanguage());
            bookStmt.setObject(7, book.getPagesCount());
            bookStmt.setString(8, book.getGenre());
            bookStmt.setString(9, book.getDescription());
            bookStmt.executeUpdate();

            try (ResultSet keys = bookStmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create book: no generated id returned.");
                }
                return keys.getLong(1);
            }
        }
    }

    private void linkBookAuthors(Connection conn, Long bookId, List<Author> authors, boolean createMissingAuthors) throws SQLException {
        if (authors == null || authors.isEmpty()) {
            return;
        }

        String insertBookAuthorSql = "INSERT INTO book_authors (book_id, author_id) VALUES (?, ?)";
        try (PreparedStatement linkStmt = conn.prepareStatement(insertBookAuthorSql)) {
            for (Author author : authors) {
                Long authorId = resolveAuthorId(conn, author, createMissingAuthors);
                linkStmt.setLong(1, bookId);
                linkStmt.setLong(2, authorId);
                linkStmt.addBatch();
            }
            linkStmt.executeBatch();
        }
    }

    private Long resolveAuthorId(Connection conn, Author author, boolean createMissingAuthors) throws SQLException {
        if (author == null) {
            throw new SQLException("Author cannot be null.");
        }
        if (author.getId() != null) {
            return author.getId();
        }
        if (!createMissingAuthors) {
            throw new SQLException("Author id is required to create book-author link.");
        }
        if (author.getPenName() == null || author.getPenName().isBlank()) {
            throw new SQLException("Author penName is required when author id is missing.");
        }

        Long existingAuthorId = findAuthorIdByPenName(conn, author.getPenName());
        if (existingAuthorId != null) {
            return existingAuthorId;
        }
        return insertAuthor(conn, author);
    }

    private Long findAuthorIdByPenName(Connection conn, String penName) throws SQLException {
        String findSql = "SELECT id FROM authors WHERE LOWER(pen_name) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setString(1, penName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
                return null;
            }
        }
    }

    private Long insertAuthor(Connection conn, Author author) throws SQLException {
        String insertAuthorSql = "INSERT INTO authors (pen_name, biography) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertAuthorSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, author.getPenName());
            stmt.setString(2, author.getBiography());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create author: no generated id returned.");
                }
                return keys.getLong(1);
            }
        }
    }

    public List<Book> findById(String id) throws SQLException {
        String sql = "SELECT * FROM books WHERE id = ?";
        String loggerMessage = "Fetching book by id.";
        return query(sql, List.of(id), this::mapResultSetToBook, loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        String loggerMessage = "Deleting books by id.";
        return update(sql, List.of(id), loggerMessage);
    }
}

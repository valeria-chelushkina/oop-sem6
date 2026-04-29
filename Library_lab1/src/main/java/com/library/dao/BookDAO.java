package com.library.dao;

import com.library.entity.Book;
import java.sql.ResultSet;
import java.sql.SQLException;
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
        String loggerMessage = "Fetching books by title";
        return query(sql, List.of("%" + title + "%"), this::mapResultSetToBook, loggerMessage);
    }

    public List<Book> findByAuthor(String author) throws SQLException {
        String sql = "SELECT DISTINCT b.* FROM books b " +
                "LEFT JOIN book_authors ba ON b.id = ba.book_id " +
                "LEFT JOIN authors a ON ba.author_id = a.id " +
                "WHERE LOWER(a.pen_name) LIKE LOWER(?)";
        String loggerMessage = "Fetching books by author";
        return query(sql, List.of("%" + author + "%"), this::mapResultSetToBook, loggerMessage);
    }

    public List<Book> findByGenre(String genre) throws SQLException {
        String sql = "SELECT * FROM books WHERE LOWER(genre) = LOWER(?)";
        String loggerMessage = "Fetching books by genre";
        return query(sql, List.of(genre), this::mapResultSetToBook, loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        String loggerMessage = "Deleting books by id";
        return update(sql, List.of(id), loggerMessage);
    }
}

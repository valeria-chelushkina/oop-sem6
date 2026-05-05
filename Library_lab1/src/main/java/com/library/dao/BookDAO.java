package com.library.dao;

import com.library.entity.Author;
import com.library.entity.Book;
import com.library.entity.Genre;
import com.library.util.DatabaseManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookDAO extends BaseDAO {
    private static final String BASE_SELECT_WITH_RELATIONS =
            "SELECT b.*, " +
                    "(SELECT ROUND(AVG(br.rating)::numeric, 2) FROM book_ratings br WHERE br.book_id = b.id) AS avg_rating, " +
                    "(SELECT COUNT(*)::int FROM book_ratings br WHERE br.book_id = b.id) AS ratings_count, " +
                    "(SELECT COUNT(*)::int FROM book_items bi LEFT JOIN loans l ON bi.id = l.book_item_id " +
                    "WHERE bi.book_id = b.id AND l.status = 'RETURNED') AS times_read, " +
                    "a.id AS author_id, a.pen_name, a.biography, " +
                    "g.id AS genre_id, g.name AS genre_name " +
                    "FROM books b " +
                    "LEFT JOIN book_authors ba ON b.id = ba.book_id " +
                    "LEFT JOIN authors a ON ba.author_id = a.id " +
                    "LEFT JOIN book_genres bg ON b.id = bg.book_id " +
                    "LEFT JOIN genres g ON bg.genre_id = g.id ";

    private Book mapResultSetToBook(ResultSet rs) throws SQLException {
        Object avgObj = rs.getObject("avg_rating");
        Double averageRating = null;
        if (avgObj instanceof Number) {
            averageRating = ((Number) avgObj).doubleValue();
        }
        int ratingsCount = rs.getInt("ratings_count");
        int timesRead = rs.getInt("times_read");
        if (rs.wasNull()) {
            ratingsCount = 0;
            timesRead = 0;
        }
        return Book.builder()
                .id(rs.getLong("id"))
                .title(rs.getString("title"))
                .isbn(rs.getString("isbn"))
                .publisher(rs.getString("publisher"))
                .publicationYear(rs.getInt("publication_year"))
                .coverURL(rs.getString("cover_url"))
                .language(rs.getString("language"))
                .pagesCount(rs.getInt("pages_count"))
                .description(rs.getString("description"))
                .averageRating(averageRating)
                .ratingsCount(ratingsCount)
                .timesRead(timesRead)
                .build();
    }

    private List<Book> queryBooksWithRelations(String sql, List<Object> params, String loggerMessage) throws SQLException {
        logger.info(loggerMessage);
        Map<Long, Book> booksById = new LinkedHashMap<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Long bookId = rs.getLong("id");
                    Book book = booksById.get(bookId);
                    if (book == null) {
                        book = mapResultSetToBook(rs);
                        book.setAuthors(new ArrayList<>());
                        book.setGenres(new ArrayList<>());
                        booksById.put(bookId, book);
                    }

                    Object authorIdObj = rs.getObject("author_id");
                    Long authorId = authorIdObj == null ? null : ((Number) authorIdObj).longValue();
                    if (authorId != null) {
                        boolean alreadyAdded = book.getAuthors().stream()
                                .anyMatch(a -> a.getId() != null && a.getId().equals(authorId));
                        if (!alreadyAdded) {
                            book.getAuthors().add(Author.builder()
                                    .id(authorId)
                                    .penName(rs.getString("pen_name"))
                                    .biography(rs.getString("biography"))
                                    .build());
                        }
                    }

                    Object genreIdObj = rs.getObject("genre_id");
                    Long genreId = genreIdObj == null ? null : ((Number) genreIdObj).longValue();
                    if (genreId != null) {
                        boolean alreadyAdded = book.getGenres().stream()
                                .anyMatch(g -> g.getId() != null && g.getId().equals(genreId));
                        if (!alreadyAdded) {
                            book.getGenres().add(Genre.builder()
                                    .id(genreId)
                                    .name(rs.getString("genre_name"))
                                    .build());
                        }
                    }
                }
            }
            List<Book> books = new ArrayList<>(booksById.values());
            logger.info("Fetched {} book records", books.size());
            return books;
        } catch (SQLException e) {
            logger.error("Failed to fetch books with authors from database", e);
            throw e;
        }
    }

    public List<Book> findAll() throws SQLException {
        String sql = BASE_SELECT_WITH_RELATIONS;
        String loggerMessage = "Fetching all books from the catalogue.";
        return queryBooksWithRelations(sql, Collections.emptyList(), loggerMessage);
    }

    public List<Book> findByTitle(String title) throws SQLException {
        String sql = BASE_SELECT_WITH_RELATIONS + "WHERE LOWER(b.title) LIKE LOWER(?)";
        String loggerMessage = "Fetching books by title.";
        return queryBooksWithRelations(sql, List.of("%" + title + "%"), loggerMessage);
    }

    public List<Book> findByAuthor(String author) throws SQLException {
        String sql = BASE_SELECT_WITH_RELATIONS +
                "WHERE EXISTS (" +
                "SELECT 1 FROM book_authors ba2 " +
                "JOIN authors a2 ON a2.id = ba2.author_id " +
                "WHERE ba2.book_id = b.id AND LOWER(a2.pen_name) LIKE LOWER(?)" +
                ")";
        String loggerMessage = "Fetching books by author.";
        return queryBooksWithRelations(sql, List.of("%" + author + "%"), loggerMessage);
    }

    public List<Book> findByTitleOrAuthor(String query) throws SQLException{
        String sql = BASE_SELECT_WITH_RELATIONS +
                "WHERE LOWER(b.title) LIKE LOWER(?) " +
                "OR EXISTS (" +
                "SELECT 1 FROM book_authors ba2 " +
                "JOIN authors a2 ON a2.id = ba2.author_id " +
                "WHERE ba2.book_id = b.id AND LOWER(a2.pen_name) LIKE LOWER(?)" +
                ")";
        String loggerMessage = "Fetching books by author or title.";
        String searchPattern = "%" + query.toLowerCase() + "%";
        return queryBooksWithRelations(sql, List.of(searchPattern, searchPattern), loggerMessage);
    }

    public List<Book> findByGenre(String genre) throws SQLException {
        String sql = BASE_SELECT_WITH_RELATIONS +
                "WHERE EXISTS (" +
                "SELECT 1 FROM book_genres bg2 " +
                "JOIN genres g2 ON g2.id = bg2.genre_id " +
                "WHERE bg2.book_id = b.id AND LOWER(g2.name) LIKE LOWER(?)" +
                ")";
        String loggerMessage = "Fetching books by genre.";
        return queryBooksWithRelations(sql, List.of("%" + genre + "%"), loggerMessage);
    }

    public List<Book> findByLanguage(String language) throws SQLException {
        String sql = BASE_SELECT_WITH_RELATIONS + "WHERE LOWER(b.language) = LOWER(?)";
        String loggerMessage = "Fetching books by language.";
        return queryBooksWithRelations(sql, List.of(language), loggerMessage);
    }

    /**
     * Комбінований пошук: текст (назва або автор) AND жанри AND мови.
     * Кілька жанрів — OR (достатньо одного збігу). Кілька мов — OR.
     * Між групами «текст», «жанри», «мови» завжди AND.
     */
    public List<Book> searchBooks(String query, List<String> genres, List<String> languages) throws SQLException {
        StringBuilder sql = new StringBuilder(BASE_SELECT_WITH_RELATIONS).append("WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.isBlank()) {
            sql.append(" AND (LOWER(b.title) LIKE LOWER(?) OR EXISTS (");
            sql.append("SELECT 1 FROM book_authors ba_q JOIN authors a_q ON a_q.id = ba_q.author_id ");
            sql.append("WHERE ba_q.book_id = b.id AND LOWER(a_q.pen_name) LIKE LOWER(?)))");
            String p = "%" + query.trim().toLowerCase() + "%";
            params.add(p);
            params.add(p);
        }

        List<String> genreList = genres == null ? List.of() : genres.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (!genreList.isEmpty()) {
            sql.append(" AND EXISTS (SELECT 1 FROM book_genres bg_g JOIN genres g_g ON g_g.id = bg_g.genre_id ");
            sql.append("WHERE bg_g.book_id = b.id AND (");
            for (int i = 0; i < genreList.size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                sql.append("LOWER(g_g.name) LIKE LOWER(?)");
                params.add("%" + genreList.get(i).toLowerCase() + "%");
            }
            sql.append("))");
        }

        List<String> langList = languages == null ? List.of() : languages.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        if (!langList.isEmpty()) {
            sql.append(" AND (");
            for (int i = 0; i < langList.size(); i++) {
                if (i > 0) {
                    sql.append(" OR ");
                }
                sql.append("LOWER(b.language) = LOWER(?)");
                params.add(langList.get(i));
            }
            sql.append(")");
        }

        return queryBooksWithRelations(sql.toString(), params, "Combined book search.");
    }

    public List<String> findUniqueLanguages() throws SQLException {
        String sql = "SELECT DISTINCT language FROM books WHERE language IS NOT NULL ORDER BY language";
        String loggerMessage = "Fetching unique languages.";
        return query(sql, List.of(), rs -> rs.getString("language"), loggerMessage);
    }

    public Long create(Book book) throws SQLException {
        logger.info("Creating new book with title='{}'", book.getTitle());
        try (Connection conn = DatabaseManager.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                Long createdBookId = insertBook(conn, book);
                linkBookAuthors(conn, createdBookId, book.getAuthors(), false);
                linkBookGenres(conn, createdBookId, book.getGenres(), false);

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
                linkBookGenres(conn, createdBookId, book.getGenres(), true);

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
        String updateBookSql = "UPDATE books SET title = ?, isbn = ?, publisher = ?, publication_year = ?, cover_url = ?, language = ?, pages_count = ?, description = ? " +
                "WHERE id = ?";
        String deleteLinksSql = "DELETE FROM book_authors WHERE book_id = ?";
        String deleteGenreLinksSql = "DELETE FROM book_genres WHERE book_id = ?";

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
                    stmt.setString(8, book.getDescription());
                    stmt.setLong(9, book.getId());
                    affectedRows = stmt.executeUpdate();
                }

                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteLinksSql)) {
                    deleteStmt.setLong(1, book.getId());
                    deleteStmt.executeUpdate();
                }
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteGenreLinksSql)) {
                    deleteStmt.setLong(1, book.getId());
                    deleteStmt.executeUpdate();
                }

                linkBookAuthors(conn, book.getId(), book.getAuthors(), true);
                linkBookGenres(conn, book.getId(), book.getGenres(), true);

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
        String insertBookSql = "INSERT INTO books (title, isbn, publisher, publication_year, cover_url, language, pages_count, description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement bookStmt = conn.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
            bookStmt.setString(1, book.getTitle());
            bookStmt.setString(2, book.getIsbn());
            bookStmt.setString(3, book.getPublisher());
            bookStmt.setObject(4, book.getPublicationYear());
            bookStmt.setString(5, book.getCoverURL());
            bookStmt.setString(6, book.getLanguage());
            bookStmt.setObject(7, book.getPagesCount());
            bookStmt.setString(8, book.getDescription());
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

    private void linkBookGenres(Connection conn, Long bookId, List<Genre> genres, boolean createMissingGenres) throws SQLException {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        String insertBookGenreSql = "INSERT INTO book_genres (book_id, genre_id) VALUES (?, ?)";
        try (PreparedStatement linkStmt = conn.prepareStatement(insertBookGenreSql)) {
            for (Genre genre : genres) {
                Long genreId = resolveGenreId(conn, genre, createMissingGenres);
                linkStmt.setLong(1, bookId);
                linkStmt.setLong(2, genreId);
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

    private Long resolveGenreId(Connection conn, Genre genre, boolean createMissingGenres) throws SQLException {
        if (genre == null) {
            throw new SQLException("Genre cannot be null.");
        }
        if (genre.getId() != null) {
            return genre.getId();
        }
        if (!createMissingGenres) {
            throw new SQLException("Genre id is required to create book-genre link.");
        }
        if (genre.getName() == null || genre.getName().isBlank()) {
            throw new SQLException("Genre name is required when genre id is missing.");
        }

        Long existingGenreId = findGenreIdByName(conn, genre.getName());
        if (existingGenreId != null) {
            return existingGenreId;
        }
        return insertGenre(conn, genre);
    }

    private Long findGenreIdByName(Connection conn, String name) throws SQLException {
        String findSql = "SELECT id FROM genres WHERE LOWER(name) = LOWER(?)";
        try (PreparedStatement stmt = conn.prepareStatement(findSql)) {
            stmt.setString(1, name);
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

    private Long insertGenre(Connection conn, Genre genre) throws SQLException {
        String insertGenreSql = "INSERT INTO genres (name) VALUES (?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertGenreSql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, genre.getName());
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("Failed to create genre: no generated id returned.");
                }
                return keys.getLong(1);
            }
        }
    }

    public List<Book> findById(Long id) throws SQLException {
        String sql = BASE_SELECT_WITH_RELATIONS + "WHERE b.id = ?";
        String loggerMessage = "Fetching book by id.";
        return queryBooksWithRelations(sql, List.of(id), loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM books WHERE id = ?";
        String loggerMessage = "Deleting books by id.";
        return update(sql, List.of(id), loggerMessage);
    }
}
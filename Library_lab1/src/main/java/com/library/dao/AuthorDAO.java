package com.library.dao;

import com.library.entity.Author;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AuthorDAO extends BaseDAO {
    private Author mapResultSetToAuthor(ResultSet rs) throws SQLException {
        return Author.builder()
                .id(rs.getLong("id"))
                .penName(rs.getString("pen_name"))
                .biography(rs.getString("biography"))
                .build();
    }

    public List<Author> findAll() throws SQLException {
        String sql = "SELECT * FROM authors";
        String loggerMessage = "Fetching all authors from the catalogue.";
        return query(sql, Collections.emptyList(), this::mapResultSetToAuthor, loggerMessage);
    }

    public Long create(Author author) throws SQLException {
        String sql = "INSERT INTO authors (pen_name, biography) VALUES (?, ?)";
        String loggerMessage = "Creating new author.";
        return insertAndReturnId(sql, Arrays.asList(author.getPenName(), author.getBiography()), loggerMessage);
    }

    public int update(Author author) throws SQLException {
        String sql = "UPDATE authors SET pen_name = ?, biography = ? WHERE id = ?";
        String loggerMessage = "Updating author by id.";
        return update(sql, Arrays.asList(author.getPenName(), author.getBiography(), author.getId()), loggerMessage);
    }

    public List<Author> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM authors WHERE id = ?";
        String loggerMessage = "Fetching author by id.";
        return query(sql, List.of(id), this::mapResultSetToAuthor, loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM authors WHERE id = ?";
        String loggerMessage = "Deleting authors by id.";
        return update(sql, List.of(id), loggerMessage);
    }
}

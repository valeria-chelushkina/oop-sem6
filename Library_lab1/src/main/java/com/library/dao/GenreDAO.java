package com.library.dao;

import com.library.entity.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GenreDAO extends BaseDAO {

    private Genre mapResultSetToGenre(ResultSet rs) throws SQLException {
        return Genre.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .build();
    }

    public List<Genre> findAll() throws SQLException{
        String sql = "SELECT * FROM genres";
        String loggerMessage = "Fetching all genres.";
        return query(sql, Collections.emptyList(), this::mapResultSetToGenre, loggerMessage);
    }

    public List<String> findAllGenreNames() throws SQLException {
        String sql = "SELECT name FROM genres ORDER BY name";
        String loggerMessage = "Fetching all genre names.";
        return query(sql, List.of(), rs -> rs.getString("name"), loggerMessage);
    }

    public Long create(Genre genre) throws SQLException{
        String sql = "INSERT INTO genres (name) VALUES (?)";
        String loggerMessage = "Creating new genre.";
        return insertAndReturnId(sql, Collections.singletonList(genre.getName()), loggerMessage);
    }

    public int update(Genre genre) throws SQLException {
        String sql = "UPDATE genres SET name = ? WHERE id = ?";
        String loggerMessage = "Updating genre by id.";
        return update(sql, Arrays.asList(genre.getName(), genre.getId()), loggerMessage);
    }

    public List<Genre> findById(Long id) throws SQLException{
        String sql = "SELECT * FROM genres WHERE id = ?";
        String loggerMessage = "Fetching genres by id.";
        return query(sql, List.of(id), this::mapResultSetToGenre, loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM genres WHERE id = ?";
        String loggerMessage = "Deleting genres by id.";
        return update(sql, List.of(id), loggerMessage);
    }

}
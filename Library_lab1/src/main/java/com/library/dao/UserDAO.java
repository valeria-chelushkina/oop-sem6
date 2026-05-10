package com.library.dao;

import com.library.entity.User;
import com.library.entity.enums.UserRole;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UserDAO extends BaseDAO {
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return User.builder()
                .id(rs.getLong("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .role(UserRole.valueOf(rs.getString("role")))
                .registrationDate(rs.getTimestamp("registration_date").toLocalDateTime())
                .build();
    }

    public List<User> findAll() throws SQLException {
        String sql = "SELECT * FROM users";
        String loggerMessage = "Fetching all users from the catalogue.";
        return query(sql, Collections.emptyList(), this::mapResultSetToUser, loggerMessage);
    }

    public Long create(User user) throws SQLException {
        String sql = "INSERT INTO users (first_name, last_name, email, role, registration_date) " +
                "VALUES (?, ?, ?, ?, ?)";
        String loggerMessage = "Creating new user.";
        return insertAndReturnId(
                sql,
                Arrays.asList(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole() != null ? user.getRole().name() : null,
                        user.getRegistrationDate()
                ),
                loggerMessage
        );
    }

    public int update(User user) throws SQLException {
        String sql = "UPDATE users SET first_name = ?, last_name = ?, email = ?, role = ?, registration_date = ? " +
                "WHERE id = ?";
        String loggerMessage = "Updating user by id.";
        return update(
                sql,
                Arrays.asList(
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail(),
                        user.getRole() != null ? user.getRole().name() : null,
                        user.getRegistrationDate(),
                        user.getId()
                ),
                loggerMessage
        );
    }

    public List<User> findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM users WHERE email = ?";
        String loggerMessage = "Fetching user by email.";
        return query(sql, List.of(email), this::mapResultSetToUser, loggerMessage);
    }

    public List<User> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";
        String loggerMessage = "Fetching user by id.";
        return query(sql, List.of(id), this::mapResultSetToUser, loggerMessage);
    }

    public int deleteById(Long id) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        String loggerMessage = "Deleting users by id.";
        return update(sql, List.of(id), loggerMessage);
    }
}
package com.library.dao;

import com.library.util.DatabaseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO {
    protected final Logger logger = LogManager.getLogger(getClass());

    @FunctionalInterface
    protected interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    protected <T> List<T> query(String sql, List<Object> params, RowMapper<T> mapper, String logMessage) throws SQLException {
        logger.info(logMessage);
        List<T> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
            }
            logger.info("Fetched {} record(s)", result.size());
            return result;
        } catch (SQLException e) {
            logger.error("Query failed", e);
            throw e;
        }
    }

    protected int update(String sql, List<Object> params, String logMessage) throws SQLException {
        logger.info(logMessage);
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, params);
            int affectedRows = stmt.executeUpdate();
            logger.info("Affected rows: {}", affectedRows);
            return affectedRows;
        } catch (SQLException e) {
            logger.error("Update failed", e);
            throw e;
        }
    }

    protected Long insertAndReturnId(String sql, List<Object> params, String logMessage) throws SQLException {
        logger.info(logMessage);
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindParams(stmt, params);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    Long id = keys.getLong(1);
                    logger.info("Created record with id={}", id);
                    return id;
                }
                throw new SQLException("Insert succeeded but no generated id was returned.");
            }
        } catch (SQLException e) {
            logger.error("Insert failed", e);
            throw e;
        }
    }

    protected int queryForInt(String sql, List<Object> params, String logMessage) throws SQLException {
        logger.info(logMessage);
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            bindParams(stmt, params);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int value = rs.getInt(1);
                    logger.info("Query returned value: {}", value);
                    return value;
                }
                return 0;
            }
        } catch (SQLException e) {
            logger.error("Scalar query failed", e);
            throw e;
        }
    }

    private void bindParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }
}
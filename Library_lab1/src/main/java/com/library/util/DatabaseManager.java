package com.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final HikariDataSource dataSource;

    private DatabaseManager() throws SQLException {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                throw new SQLException("db.properties file not found");
            }

            Properties prop = new Properties();
            prop.load(input);
            Class.forName(prop.getProperty("db.driver"));

            String passwordFromEnv = System.getenv("DB_PASSWORD");
            String password = (passwordFromEnv != null && !passwordFromEnv.isBlank())
                    ? passwordFromEnv
                    : prop.getProperty("db.password", "");

            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(prop.getProperty("db.url"));
            config.setUsername(prop.getProperty("db.user"));
            config.setPassword(password);
            config.setDriverClassName(prop.getProperty("db.driver"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setPoolName("library-pool");
            config.setAutoCommit(true);

            this.dataSource = new HikariDataSource(config);
        } catch (IOException e) {
            throw new SQLException("Cannot read database config: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new SQLException("Database connection error: " + e.getMessage(), e);
        }
    }

    public static synchronized DatabaseManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
package com.library.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseManager {
    private static final Logger logger = LogManager.getLogger(DatabaseManager.class);
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
            if (password == null || password.isBlank()) {
                throw new SQLException("Database password is not set. Configure DB_PASSWORD environment variable or set db.password in db.properties for local tests.");
            }

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
            logger.info("Database connection pool initialized for {}", prop.getProperty("db.url"));
        } catch (IOException e) {
            logger.error("Cannot read db.properties", e);
            throw new SQLException("Cannot read database config: " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Database initialization error", e);
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
        logger.debug("Providing a database connection from pool");
        return dataSource.getConnection();
    }
}
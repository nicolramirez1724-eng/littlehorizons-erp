package com.littlehorizons.erp.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton database connection manager reading configuration from database.properties.
 * Ensures robust connection retrieval and graceful fallback handling.
 */
public class DatabaseConnection {

    private static DatabaseConnection instance;
    private String url;
    private String user;
    private String password;

    private DatabaseConnection() {
        loadConfiguration();
    }

    private void loadConfiguration() {
        Properties properties = new Properties();
        File propFile = new File("database.properties");
        if (propFile.exists()) {
            try (FileInputStream fis = new FileInputStream(propFile)) {
                properties.load(fis);
                this.url = properties.getProperty("db.url", "jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8");
                this.user = properties.getProperty("db.user", "root");
                this.password = properties.getProperty("db.password", "");
            } catch (IOException e) {
                LoggerManager.logError("Failed to read database.properties, applying defaults", e);
                setDefaultCredentials();
            }
        } else {
            setDefaultCredentials();
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LoggerManager.logError("MySQL JDBC Driver not found in classpath", e);
        }
    }

    private void setDefaultCredentials() {
        this.url = "jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8";
        this.user = "root";
        this.password = "";
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Obtains a new database connection.
     * Callers must close it via try-with-resources.
     *
     * @return SQL Connection instance
     * @throws SQLException if a connection cannot be established
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(this.url, this.user, this.password);
    }

    /**
     * Verifies if database connection is functional.
     *
     * @return true if connection succeeds, false otherwise
     */
    public boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LoggerManager.logError("Database connection test failed", e);
            return false;
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUser() {
        return user;
    }
}

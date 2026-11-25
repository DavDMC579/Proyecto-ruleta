package com.roulette.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Manages database configuration and JDBC connections to PostgreSQL (RQ08).
 */
public class DatabaseConfig {
    private static final Properties properties = new Properties();
    private static boolean driverLoaded = false;

    static {
        try (InputStream in = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (in != null) {
                properties.load(in);
            } else {
                properties.setProperty("db.url", "jdbc:postgresql://localhost:5432/roulette_db");
                properties.setProperty("db.user", "postgres");
                properties.setProperty("db.password", "postgres");
                properties.setProperty("db.driver", "org.postgresql.Driver");
            }

            Class.forName(properties.getProperty("db.driver", "org.postgresql.Driver"));
            driverLoaded = true;
        } catch (Exception e) {
            // Driver may not be loaded if testing in standalone mode
            driverLoaded = false;
        }
    }

    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            throw new SQLException("PostgreSQL driver not loaded");
        }
        return DriverManager.getConnection(
                properties.getProperty("db.url"),
                properties.getProperty("db.user"),
                properties.getProperty("db.password")
        );
    }

    public static boolean isConnected() {
        try {
            DriverManager.setLoginTimeout(2);
            try (Connection conn = getConnection()) {
                return conn != null && !conn.isClosed();
            }
        } catch (Exception e) {
            return false;
        }
    }
}

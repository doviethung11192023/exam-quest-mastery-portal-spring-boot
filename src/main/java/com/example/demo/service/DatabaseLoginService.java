package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Service
public class DatabaseLoginService {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Autowired
    private DataSource dataSource;

    /**
     * Validates if the provided credentials can connect to the database
     */
    public boolean validateDatabaseAccess(String username, String password) {
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
            // If connection is successful, credentials are valid
            return true;
        } catch (SQLException e) {
            System.err.println("Database validation failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * Updates the application's database connection pool with new credentials
     */
    public void updateDatabaseCredentials(String username, String password) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;

            try {
                // This will affect new connections from the pool
                hikariDataSource.setUsername(username);
                hikariDataSource.setPassword(password);

                // Force clear current connections to apply new credentials
                hikariDataSource.getHikariPoolMXBean().softEvictConnections();

                System.out.println("Database credentials updated successfully for user: " + username);
            } catch (Exception e) {
                System.err.println("Failed to update database credentials: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            throw new IllegalStateException("DataSource is not an instance of HikariDataSource");
        }
    }

    /**
     * Reset to default database credentials (for logout)
     */
    // public void resetDatabaseCredentials(String defaultUsername, String defaultPassword) {
    //     updateDatabaseCredentials(defaultUsername, defaultPassword);
    // }
    public void resetDatabaseCredentials(String defaultUsername, String defaultPassword) {
        try {
            if (dataSource instanceof HikariDataSource) {
                HikariDataSource hikariDS = (HikariDataSource) dataSource;

                // Don't close the pool completely - just update its credentials
                hikariDS.setUsername(defaultUsername);
                hikariDS.setPassword(defaultPassword);

                // Soft evict connections to force new ones to be created with updated
                // credentials
                hikariDS.getHikariPoolMXBean().softEvictConnections();

                System.out.println("Database connection pool reset with default credentials");
            }
        } catch (Exception e) {
            System.err.println("Error resetting database credentials: " + e.getMessage());
            e.printStackTrace();
        }
    }
// Helper method to update the dataSource using reflection if needed
private void updateDataSourceField(HikariDataSource newDs) {
    try {
        // Use reflection to replace the existing dataSource with the new one
        // This approach depends on your specific implementation
        Field field = this.getClass().getDeclaredField("dataSource");
        field.setAccessible(true);
        field.set(this, newDs);
    } catch (Exception e) {
        System.err.println("Failed to update dataSource using reflection: " + e.getMessage());
    }
}
}
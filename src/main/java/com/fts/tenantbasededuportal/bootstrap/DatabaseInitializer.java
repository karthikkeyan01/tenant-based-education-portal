package com.fts.tenantbasededuportal.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class DatabaseInitializer {

    private DatabaseInitializer() {
    }

    public static void initializeDatabase() {
        final Properties properties = new Properties();

        try (InputStream inputStream = DatabaseInitializer.class.getClassLoader().getResourceAsStream(
                "application.properties")) {
            if (inputStream == null) {
                throw new RuntimeException("application.properties not found.");
            }
            properties.load(inputStream);
        } catch (final IOException exception) {
            throw new RuntimeException("Unable to load application.properties.", exception);
        }

        final String datasourceUrl = properties.getProperty("spring.datasource.url");
        final String username = properties.getProperty("spring.datasource.username");
        final String password = properties.getProperty("spring.datasource.password");

        if (datasourceUrl == null || username == null || password == null) {
            throw new RuntimeException("Datasource properties are missing.");
        }

        final String databaseNameWithParams = datasourceUrl.substring(datasourceUrl.lastIndexOf("/") + 1);
        final String databaseName = databaseNameWithParams.split("\\?")[0];
        final String postgresUrl = datasourceUrl.replace(databaseName, "postgres");

        try (Connection connection = DriverManager.getConnection(postgresUrl, username, password)) {
            final String query = "SELECT 1 FROM pg_database WHERE datname = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, databaseName);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (!resultSet.next()) {
                        final String createDatabaseQuery = "CREATE DATABASE \"" + databaseName + "\"";

                        try (Statement statement = connection.createStatement()) {
                            statement.executeUpdate(createDatabaseQuery);
                            log.info("Database '{}' created successfully.", databaseName);
                        }
                    } else {
                        log.info("Database '{}' already exists.", databaseName);
                    }
                }
            }
        } catch (final SQLException exception) {
            throw new RuntimeException("Failed to initialize database: " + exception.getMessage(), exception);
        }
    }
}
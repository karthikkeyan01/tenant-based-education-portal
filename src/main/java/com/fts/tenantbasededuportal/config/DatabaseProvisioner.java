package com.fts.tenantbasededuportal.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
@Slf4j
public class DatabaseProvisioner {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    public void provisionDatabase() throws Exception {

        final String databaseName = this.datasourceUrl.substring(this.datasourceUrl.lastIndexOf("/") + 1).split("\\?")[0];
        try {
            log.info("Ensuring database '{}' exists.", databaseName);
            final String postgresUrl = this.datasourceUrl.replace(databaseName, "postgres");

            try (final Connection connection = DriverManager.getConnection(postgresUrl, this.username, this.password)){

                final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                try (final Liquibase liquibase = new Liquibase("db/changelog/create-db/db.changelog-create-db.xml",
                        new ClassLoaderResourceAccessor(), database)){

                    liquibase.getChangeLogParameters().set("databaseName", databaseName);
                    log.info("Running database provisioning changelog for database '{}'.", databaseName);
                    liquibase.update();
                    log.info("Database provisioning completed for '{}'.", databaseName);
                }
            }
        }
        catch (final Exception exception) {
            log.error("Failed to provision database '{}'.", databaseName, exception);
            throw exception;
        }
    }
}

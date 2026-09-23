package com.fts.tenantbasededuportal.config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;

@Component
@Slf4j
@RequiredArgsConstructor
public class DatabaseProvisioner {

    private final Environment environment;
    @Value("${spring.datasource.url}")
    private String datasourceUrl;
    @Value("${app.database-provisioner.url}")
    private String databaseProvisionerUrl;
    @Value("${app.database-provisioner.username}")
    private String databaseProvisionerUsername;
    @Value("${app.database-provisioner.password}")
    private String databaseProvisionerPassword;

    public void provisionDatabase() throws Exception {

        final String databaseName = this.datasourceUrl.substring(this.datasourceUrl.lastIndexOf("/") + 1).split("\\?")[0];
        final String activeProfile = this.environment.getActiveProfiles().length > 0
                ? this.environment.getActiveProfiles()[0] : "default";
        try {
            log.info("Ensuring database '{}' exists.", databaseName);
            final String provisionerUrl = this.databaseProvisionerUrl;

            try (final Connection connection = DriverManager.getConnection(provisionerUrl, this.databaseProvisionerUsername, this.databaseProvisionerPassword)){

                final Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

                final String changeLogTable = "DATABASECHANGELOG_PROVISIONER_" + activeProfile.toUpperCase();
                final String changeLogLockTable = "DATABASECHANGELOGLOCK_PROVISIONER_" + activeProfile.toUpperCase();
                database.setDatabaseChangeLogTableName(changeLogTable);
                database.setDatabaseChangeLogLockTableName(changeLogLockTable);

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
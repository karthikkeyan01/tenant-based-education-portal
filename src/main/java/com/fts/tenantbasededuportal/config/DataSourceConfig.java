package com.fts.tenantbasededuportal.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    private final DatabaseProvisioner databaseProvisioner;

    @Bean
    public DataSource dataSource() throws Exception {

        this.databaseProvisioner.provisionDatabase();

        return DataSourceBuilder.create()
                .url(this.datasourceUrl)
                .username(this.username)
                .password(this.password)
                .type(HikariDataSource.class)
                .build();
    }
}
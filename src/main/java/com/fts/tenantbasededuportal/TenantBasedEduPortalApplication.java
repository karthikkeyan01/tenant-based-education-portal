package com.fts.tenantbasededuportal;

import com.fts.tenantbasededuportal.bootstrap.DatabaseInitializer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TenantBasedEduPortalApplication {

	public static void main(String[] args) {
		DatabaseInitializer.initializeDatabase();
		SpringApplication.run(TenantBasedEduPortalApplication.class, args);
	}

}

package com.fts.tenantbasededuportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TenantBasedEduPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(TenantBasedEduPortalApplication.class, args);
	}

}

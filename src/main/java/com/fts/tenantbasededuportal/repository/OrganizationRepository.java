package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository
        extends JpaRepository<Organization, String> {

    Optional<Organization> findByName(String name);

    boolean existsByName(String name);
}

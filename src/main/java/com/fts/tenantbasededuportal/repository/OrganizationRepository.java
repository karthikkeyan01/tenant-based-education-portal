package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrganizationRepository
        extends JpaRepository<Organization, String> {

    boolean existsByName(String name);

    List<Organization> findByActiveTrue();

    Optional<Organization> findByName(String name);

    boolean existsByNameAndActiveTrue(String name);

    Optional<Organization> findByIdAndActiveTrue(String id);
}

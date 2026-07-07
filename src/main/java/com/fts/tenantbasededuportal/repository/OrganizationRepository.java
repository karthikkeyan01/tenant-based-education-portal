package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrganizationRepository
        extends JpaRepository<Organization, String> {

    boolean existsByName(String name);

    Page<Organization> findByActiveTrue(Pageable pageable);

    Optional<Organization> findByIdAndActiveTrue(String id);
}

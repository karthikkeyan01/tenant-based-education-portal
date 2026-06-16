package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, String> {

    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

}
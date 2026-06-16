package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionRepository
        extends JpaRepository<RolePermission, String> {

    List<RolePermission> findByRole(Role role);
}

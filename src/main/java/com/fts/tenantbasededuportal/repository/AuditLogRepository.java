package com.fts.tenantbasededuportal.repository;

import com.fts.tenantbasededuportal.entity.AuditLog;
import com.fts.tenantbasededuportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository
        extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByUser(User user);
}

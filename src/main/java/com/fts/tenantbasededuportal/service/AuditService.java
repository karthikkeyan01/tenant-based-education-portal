package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.entity.AuditLog;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    private final HttpServletRequest request;

    public void log(final User user,
                    final String action,
                    final String entityAffected,
                    final String entityId,
                    final String details){

        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isBlank()) {

            ipAddress = request.getRemoteAddr();
        }

        final AuditLog auditLog = AuditLog.builder()
                .user(user)
                .action(action)
                .entityAffected(entityAffected)
                .entityId(entityId)
                .details(details)
                .ipAddress(ipAddress)
                .httpMethod(request.getMethod())
                .requestUrl(request.getRequestURI())
                .build();

        this.auditLogRepository.save(auditLog);
    }

    public Page<AuditLog> getAuditLogs(final int page, final int size){

        return this.auditLogRepository.findAll
                (PageRequest.of(page, size));
    }

    public Page<AuditLog> getAuditLogsByUser
            (final String userId, final int page, final int size){

        return this.auditLogRepository.findByUser_Id(userId,
                PageRequest.of(page, size));
    }
}

package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.audit.AuditResponseDto;
import com.fts.tenantbasededuportal.entity.AuditLog;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.constants.PermissionConstants;
import com.fts.tenantbasededuportal.repository.AuditLogRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtil securityUtil;
    private final HttpServletRequest httpRequest;
    private final PermissionService permissionService;
    private final UserRepository userRepository;

    @Transactional
    public void create(final AuditRequestDto request) {

        final User currentUser = this.securityUtil.getCurrentUser();
        final String forwardedIp = this.httpRequest.getHeader("X-Forwarded-For");
        final String ipAddress = (forwardedIp == null || forwardedIp.isBlank())
                ? this.httpRequest.getRemoteAddr()
                : forwardedIp;

        final AuditLog auditLog = AuditLog.builder()
                .user(currentUser)
                .action(request.getAction())
                .entityAffected(request.getEntityAffected())
                .entityId(request.getEntityId())
                .userAgent(this.httpRequest.getHeader("User-Agent"))
                .details(request.getDescription())
                .ipAddress(ipAddress)
                .method(this.httpRequest.getMethod())
                .requestUrl(this.httpRequest.getRequestURI())
                .build();

        this.auditLogRepository.save(auditLog);
    }

    @Transactional(readOnly = true)
    public Page<AuditResponseDto> retrieveAuditLogs(final int page, final int size) {

        log.info("Audit log retrieval requested.");
        this.permissionService.requirePermission(PermissionConstants.VIEW_AUDIT_LOGS);

        final Page<AuditLog> auditLogs = this.auditLogRepository.findAll(PageRequest.of(page, size,
                Sort.by("createdAt").descending()));
        log.info("Audit logs retrieved successfully.");

        return auditLogs.map(auditLog -> {
            final User user = auditLog.getUser();
            return AuditResponseDto.builder()
                    .id(auditLog.getId())
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .action(auditLog.getAction())
                    .entityAffected(auditLog.getEntityAffected())
                    .entityId(auditLog.getEntityId())
                    .userAgent(auditLog.getUserAgent())
                    .description(auditLog.getDetails())
                    .ipAddress(auditLog.getIpAddress())
                    .requestUrl(auditLog.getRequestUrl())
                    .method(auditLog.getMethod())
                    .createdAt(auditLog.getCreatedAt())
                    .build();
        });
    }

    @Transactional(readOnly = true)
    public Page<AuditResponseDto> retrieveAuditLogsByUser(final String userId, final int page, final int size) {

        log.info("Audit log retrieval requested for user ID '{}'.", userId);
        this.permissionService.requirePermission(PermissionConstants.VIEW_AUDIT_LOGS);
        this.userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("User not found"));

        final Page<AuditLog> auditLogs = this.auditLogRepository.findByUser_Id(userId,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()));
        log.info("Audit logs retrieved successfully for user ID '{}'.", userId);

        return auditLogs.map(auditLog -> {
            final User user = auditLog.getUser();
            return AuditResponseDto.builder()
                    .id(auditLog.getId())
                    .userId(user.getId())
                    .userEmail(user.getEmail())
                    .action(auditLog.getAction())
                    .entityAffected(auditLog.getEntityAffected())
                    .entityId(auditLog.getEntityId())
                    .userAgent(auditLog.getUserAgent())
                    .description(auditLog.getDetails())
                    .ipAddress(auditLog.getIpAddress())
                    .requestUrl(auditLog.getRequestUrl())
                    .method(auditLog.getMethod())
                    .createdAt(auditLog.getCreatedAt())
                    .build();
        });

    }
}

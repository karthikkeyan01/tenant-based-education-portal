package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.audit.AuditResponseDto;
import com.fts.tenantbasededuportal.entity.AuditLog;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    private final HttpServletRequest request;

    public void log(final AuditRequestDto requestDto){

        String ipAddress = this.request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isBlank()) {

            ipAddress = request.getRemoteAddr();
        }

        final AuditLog auditLog = AuditLog.builder()
                .user(requestDto.getUser())
                .action(requestDto.getAction())
                .entityAffected(requestDto.getEntityAffected())
                .entityId(requestDto.getEntityId())
                .details(requestDto.getDescription())
                .ipAddress(ipAddress)
                .httpMethod(request.getMethod())
                .requestUrl(request.getRequestURI())
                .build();

        this.auditLogRepository.save(auditLog);
    }

    public Page<AuditResponseDto> getAuditLogs(final int page, final int size){

        final Page<AuditLog> auditLogs =
                this.auditLogRepository.findAll(PageRequest.of
                        (page, size,
                                Sort.by("createdAt").descending()));

        return auditLogs.map(auditLog ->
                AuditResponseDto.builder()
                        .id(auditLog.getId())
                        .userId(auditLog.getUser().getId())
                        .userEmail(auditLog.getUser().getEmail())
                        .action(auditLog.getAction())
                        .entityAffected(auditLog.getEntityAffected())
                        .entityId(auditLog.getEntityId())
                        .description(auditLog.getDetails())
                        .ipAddress(auditLog.getIpAddress())
                        .requestUrl(auditLog.getRequestUrl())
                        .httpMethod(auditLog.getHttpMethod())
                        .createdAt(auditLog.getCreatedAt())
                        .build());
    }

    public Page<AuditResponseDto> getAuditLogsByUser(
            final String userId,
            final int page,
            final int size) {

        final Page<AuditLog> auditLogs =
                this.auditLogRepository.findByUser_Id(
                        userId,
                        PageRequest.of(
                                page,
                                size,
                                Sort.by("createdAt").descending()));

        return auditLogs.map(auditLog ->
                AuditResponseDto.builder()
                        .id(auditLog.getId())
                        .userId(auditLog.getUser().getId())
                        .userEmail(auditLog.getUser().getEmail())
                        .action(auditLog.getAction())
                        .entityAffected(auditLog.getEntityAffected())
                        .entityId(auditLog.getEntityId())
                        .description(auditLog.getDetails())
                        .ipAddress(auditLog.getIpAddress())
                        .requestUrl(auditLog.getRequestUrl())
                        .httpMethod(auditLog.getHttpMethod())
                        .createdAt(auditLog.getCreatedAt())
                        .build());

    }
}

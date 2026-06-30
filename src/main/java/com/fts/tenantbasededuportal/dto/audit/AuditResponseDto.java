package com.fts.tenantbasededuportal.dto.audit;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDateTime;

@Getter
@Builder
public class AuditResponseDto {

    private String id;

    private String userId;

    private String userEmail;

    private String action;

    private String entityAffected;

    private String entityId;

    private String description;

    private String ipAddress;

    private String requestUrl;

    private String httpMethod;

    private Instant createdAt;
}
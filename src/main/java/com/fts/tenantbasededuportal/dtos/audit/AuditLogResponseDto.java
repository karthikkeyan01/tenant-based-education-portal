package com.fts.tenantbasededuportal.dtos.audit;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLogResponseDto {

    private String id;

    private String userEmail;

    private String action;

    private String entityAffected;

    private String entityId;

    private String details;

    private String ipAddress;

    private String requestUrl;

    private String httpMethod;

    private Instant createdAt;
}
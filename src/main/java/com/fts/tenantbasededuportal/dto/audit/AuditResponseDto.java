package com.fts.tenantbasededuportal.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Schema(description = "Audit log details.")
@Getter
@Builder
public class AuditResponseDto {

    @Schema(description = "Unique identifier of the audit log.", example = "68b4f8f2d9a1c4e7b3f2a1d5")
    private String id;

    @Schema(description = "Unique identifier of the user who performed the action.", example = "68b4f8f2d9a1c4e7b3f2a1d6")
    private String userId;

    @Schema(description = "Email address of the user who performed the action.", example = "john.doe@example.com")
    private String userEmail;

    @Schema(description = "User agent of the client that performed the action.", example = "Mozilla/5.0")
    private String userAgent;

    @Schema(description = "Action performed.", example = "CREATE_USER")
    private String action;

    @Schema(description = "Resource affected by the action.", example = "USER")
    private String entityAffected;

    @Schema(description = "Unique identifier of the affected resource.", example = "68b4f8f2d9a1c4e7b3f2a1d5")
    private String entityId;

    @Schema(description = "Description of the audited action.", example = "User account created successfully.")
    private String description;

    @Schema(description = "IP address from which the action was performed.", example = "192.168.1.10")
    private String ipAddress;

    @Schema(description = "Request URL that triggered the action.", example = "/users")
    private String requestUrl;

    @Schema(description = "HTTP method used for the request.", example = "POST")
    private String method;

    @Schema(description = "Date and time when the audit log was created.", example = "2026-07-17T14:30:15Z")
    private Instant createdAt;
}
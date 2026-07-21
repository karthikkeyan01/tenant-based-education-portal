package com.fts.tenantbasededuportal.dto.audit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Request payload for creating an audit log entry.")
@Getter
@Builder
public class AuditRequestDto {

    @Schema(description = "Action performed.", example = "CREATE_USER")
    private String action;

    @Schema(description = "Resource affected by the action.", example = "USER")
    private String entityAffected;
    @Schema(description = "Unique identifier of the affected resource.", example = "68b4f8f2d9a1c4e7b3f2a1d5")
    private String entityId;

    @Schema(description = "Description of the audited action.", example = "User account created successfully.")
    private String description;
}

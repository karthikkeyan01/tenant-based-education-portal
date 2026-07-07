package com.fts.tenantbasededuportal.dto.audit;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditRequestDto {

    private String action;

    private String entityAffected;

    private String entityId;

    private String description;
}

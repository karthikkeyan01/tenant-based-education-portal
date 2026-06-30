package com.fts.tenantbasededuportal.dto.audit;

import com.fts.tenantbasededuportal.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuditRequestDto {

    private User user;

    private String action;

    private String entityAffected;

    private String entityId;

    private String description;
}

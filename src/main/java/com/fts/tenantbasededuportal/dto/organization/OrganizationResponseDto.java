package com.fts.tenantbasededuportal.dto.organization;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {

    private String id;

    private String name;

    private boolean active;

    private Instant createdAt;
}

package com.fts.tenantbasededuportal.dto.organization;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationResponseDto {

    private String organizationId;

    private String organizationName;

    private boolean orgActiveStatus;

    private String orgAdminEmail;

    private String orgAdminFirstName;

    private String orgAdminLastName;

    private boolean orgAdminActiveStatus;

    private Instant createdAt;
}

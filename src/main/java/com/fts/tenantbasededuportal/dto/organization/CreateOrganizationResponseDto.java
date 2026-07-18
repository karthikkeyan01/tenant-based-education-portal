package com.fts.tenantbasededuportal.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Schema(description = "Response returned after successfully creating an organization.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationResponseDto {

    @Schema(description = "Unique identifier of the organization.", example = "68b4f8f2d9a1c4e7b3f2a1d5")
    private String organizationId;

    @Schema(description = "Name of the organization.", example = "ABC University")
    private String organizationName;

    @Schema(description = "Indicates whether the organization is active.", example = "true")
    private boolean orgActiveStatus;

    @Schema(description = "Email address of the organization administrator.", example = "admin@abcuniversity.edu")
    private String orgAdminEmail;

    @Schema(description = "First name of the organization administrator.", example = "John")
    private String orgAdminFirstName;

    @Schema(description = "Last name of the organization administrator.", example = "Doe")
    private String orgAdminLastName;

    @Schema(description = "Indicates whether the organization administrator account is active.", example = "false")
    private boolean orgAdminActiveStatus;

    @Schema(description = "Date and time when the organization was created.", example = "2026-07-17T14:30:15Z")
    private Instant createdAt;
}

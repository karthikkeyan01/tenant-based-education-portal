package com.fts.tenantbasededuportal.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Schema(description = "Organization response")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponseDto {

    @Schema(description = "Unique identifier of the organization.", example = "68b4f8f2d9a1c4e7b3f2a1d5")
    private String id;

    @Schema(description = "Name of the organization.", example = "ABC University")
    private String name;

    @Schema(description = "Indicates whether the organization is active.", example = "true")
    private boolean active;

    @Schema(description = "Date and time when the organization was created.", example = "2026-07-17T14:30:15Z")
    private Instant createdAt;
}

package com.fts.tenantbasededuportal.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Schema(description = "Request payload for updating an organization.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationRequestDto {

    @Schema(description = "Name of the organization.", example = "ABC University")
    @NotBlank
    private String name;

}

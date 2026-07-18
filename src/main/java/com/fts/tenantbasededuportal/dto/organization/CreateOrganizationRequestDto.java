package com.fts.tenantbasededuportal.dto.organization;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Schema(description = "Request payload for creating a new organization and its administrator.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationRequestDto {

    @Schema(description = "Name of the organization.", example = "ABC University")
    @NotBlank
    private String organizationName;

    @Schema(description = "Email address of the organization administrator.", example = "admin@abcuniversity.edu")
    @NotBlank
    @Email
    private String orgAdminEmail;

    @Schema(description = "First name of the organization administrator.", example = "John")
    private String orgAdminFirstName;

    @Schema(description = "Last name of the organization administrator.", example = "Doe")
    private String orgAdminLastName;

}

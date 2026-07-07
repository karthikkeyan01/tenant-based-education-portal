package com.fts.tenantbasededuportal.dto.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrganizationRequestDto {

    @NotBlank
    private String organizationName;

    @NotBlank
    @Email
    private String orgAdminEmail;

    private String orgAdminFirstName;

    private String orgAdminLastName;

}

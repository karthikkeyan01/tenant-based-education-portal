package com.fts.tenantbasededuportal.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDto {

    private String email;

    private String firstName;

    private String secondName;

    private String organizationId;

    private Boolean mfaEnabled;

    private String roleName;
}
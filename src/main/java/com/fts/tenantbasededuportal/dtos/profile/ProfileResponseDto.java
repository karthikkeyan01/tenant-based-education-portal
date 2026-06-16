package com.fts.tenantbasededuportal.dtos.profile;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {

    private String id;

    private String email;

    private String firstName;

    private String secondName;

    private String roleName;

    private String organizationName;

    private Boolean mfaEnabled;
}
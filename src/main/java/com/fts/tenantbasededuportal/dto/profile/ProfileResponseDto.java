package com.fts.tenantbasededuportal.dto.profile;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {

    private String id;

    private String email;

    private String firstName;

    private String lastName;

    private String roleName;

    private String organizationName;

    private Boolean mfaEnabled;

    private Instant createdAt;
}
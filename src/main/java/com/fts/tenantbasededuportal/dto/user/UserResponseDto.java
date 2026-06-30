package com.fts.tenantbasededuportal.dto.user;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private String id;

    private String email;

    private String firstName;

    private String lastName;

    private String roleName;

    private String organizationName;

    private Boolean active;

    private Boolean mfaEnabled;

    private Instant createdAt;
}
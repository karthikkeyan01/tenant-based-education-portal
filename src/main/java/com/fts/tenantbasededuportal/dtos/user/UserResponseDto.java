package com.fts.tenantbasededuportal.dtos.user;

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

    private String secondName;

    private String roleName;

    private String organizationName;

    private Boolean deleted;

    private Boolean mfaEnabled;

    private Instant createdAt;
}
package com.fts.tenantbasededuportal.dtos.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    private String accessToken;

    private String email;

    private String role;

    private Boolean mfaRequired;
}

package com.fts.tenantbasededuportal.dto.auth;

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

    private String message;
}

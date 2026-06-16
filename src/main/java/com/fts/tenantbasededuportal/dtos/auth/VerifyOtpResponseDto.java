package com.fts.tenantbasededuportal.dtos.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyOtpResponseDto {

    private String accessToken;

    private String email;

    private String role;
}
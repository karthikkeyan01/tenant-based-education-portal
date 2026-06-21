package com.fts.tenantbasededuportal.dtos.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyMfaRequestDto {

    private String email;

    private String otp;
}

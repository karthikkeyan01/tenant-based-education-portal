package com.fts.tenantbasededuportal.dto.auth;

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

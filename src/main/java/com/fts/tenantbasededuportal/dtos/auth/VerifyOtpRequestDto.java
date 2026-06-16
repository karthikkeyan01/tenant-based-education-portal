package com.fts.tenantbasededuportal.dtos.auth;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VerifyOtpRequestDto {

    private String email;

    private String otp;
}

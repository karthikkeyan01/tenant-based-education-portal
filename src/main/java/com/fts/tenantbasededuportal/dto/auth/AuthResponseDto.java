package com.fts.tenantbasededuportal.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthResponseDto {

    private final String message;
}
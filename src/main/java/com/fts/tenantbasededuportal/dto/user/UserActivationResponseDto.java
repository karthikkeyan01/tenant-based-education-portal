package com.fts.tenantbasededuportal.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserActivationResponseDto {

    private String userId;

    private String email;

    private Boolean active;
}
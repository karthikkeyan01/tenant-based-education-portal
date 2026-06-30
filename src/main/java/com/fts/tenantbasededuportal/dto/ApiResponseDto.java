package com.fts.tenantbasededuportal.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ApiResponseDto<T> {

    private final int code;

    private final String message;

    private final T data;
}

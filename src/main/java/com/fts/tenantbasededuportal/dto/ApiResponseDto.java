package com.fts.tenantbasededuportal.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "Standard API response wrapper.")
@Getter
@Builder
@AllArgsConstructor
public class ApiResponseDto<T> {

    @Schema(description = "HTTP status code.", example = "200")
    private final int code;

    @Schema(description = "Operation result message.", example = "User retrieved successfully.")
    private final String message;

    @Schema(description = "Response payload.")
    private final T data;
}

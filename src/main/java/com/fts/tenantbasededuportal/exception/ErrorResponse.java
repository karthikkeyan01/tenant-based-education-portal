package com.fts.tenantbasededuportal.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Schema(description = "Standard API error response.")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    @Schema(description = "Timestamp when the error occurred.", example = "2026-07-17T14:30:15Z")
    private Instant timestamp;

    @Schema(description = "HTTP status code.", example = "404")
    private Integer status;

    @Schema(description = "HTTP error name.", example = "Not Found")
    private String error;

    @Schema(description = "Detailed error message.", example = "User not found.")
    private String message;

    @Schema(description = "Request path that caused the error.", example = "/users/123")
    private String path;
}

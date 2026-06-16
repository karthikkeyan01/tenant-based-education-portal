package com.fts.tenantbasededuportal.exception;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ErrorResponse {

    private Instant timestamp;

    private Integer status;

    private String error;

    private String message;

    private String path;
}

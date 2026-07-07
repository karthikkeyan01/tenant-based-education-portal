package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.audit.AuditResponseDto;
import com.fts.tenantbasededuportal.service.AuditService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Validated
public class AuditController {

    private final AuditService auditService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<AuditResponseDto>>> retrieveAuditLogs(
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size){

        final Page<AuditResponseDto> response =
                this.auditService.retrieveAuditLogs(page, size);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<AuditResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Audit logs retrieved successfully.")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponseDto<Page<AuditResponseDto>>> retrieveAuditLogsByUser(
            @PathVariable final String userId,
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size){

        final Page<AuditResponseDto> response =
                this.auditService.retrieveAuditLogsByUser(userId, page, size);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<AuditResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message("User audit logs retrieved successfully.")
                        .data(response)
                        .build());
    }
}

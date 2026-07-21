package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.audit.AuditResponseDto;
import com.fts.tenantbasededuportal.service.AuditService;
import com.fts.tenantbasededuportal.util.constants.SecurityConstants;
import com.fts.tenantbasededuportal.util.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Audit Management", description = "APIs for retrieving audit logs.")
@SecurityRequirement(name = SwaggerConstants.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
@Validated
public class AuditController {

    private final AuditService auditService;

    @ApiResponse(responseCode = "200", description = "Audit logs retrieved successfully.")
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @GetMapping
    public ApiResponseDto<Page<AuditResponseDto>> retrieveAuditLogs(
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size){

        final Page<AuditResponseDto> response = this.auditService.retrieveAuditLogs(page, size);

        return ApiResponseDto.<Page<AuditResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message("Audit logs retrieved successfully.")
                .data(response)
                .build();
    }

    @Operation(summary = "Retrieve user's audit logs", description = "Retrieves the audit logs for a specific user.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User audit logs retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "User not found.")})
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @GetMapping("/users/{userId}")
    public ApiResponseDto<Page<AuditResponseDto>> retrieveAuditLogsByUser(
            @PathVariable final String userId, @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size){

        final Page<AuditResponseDto> response = this.auditService.retrieveAuditLogsByUser(userId, page, size);

        return ApiResponseDto.<Page<AuditResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message("User audit logs retrieved successfully.")
                .data(response)
                .build();
    }
}

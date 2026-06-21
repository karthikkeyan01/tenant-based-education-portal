package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.entity.AuditLog;
import com.fts.tenantbasededuportal.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    @GetMapping
    public Page<AuditLog> getAuditLogs(
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size){

        return this.auditService.getAuditLogs(page, size);

    }

    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    @GetMapping("/{userId}")
    public Page<AuditLog> getAuditLogsByUser(
            @PathVariable final String userId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10") final int size){

        return this.auditService.getAuditLogsByUser(userId, page, size);
    }
}

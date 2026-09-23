package com.fts.tenantbasededuportal.socket.dto;

import lombok.*;

import java.time.Instant;

@Builder
public record UserCreatedSocketDto(
        String id,
        String email,
        String firstName,
        String lastName,
        String roleName,
        String organizationName,
        boolean active,
        boolean mfaEnabled,
        Instant createdAt
) {}

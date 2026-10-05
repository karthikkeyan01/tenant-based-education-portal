package com.fts.tenantbasededuportal.event;

import com.fts.tenantbasededuportal.socket.dto.UserCreatedSocketDto;

public record UserCreatedEvent(UserCreatedSocketDto user, String organizationId) {}

package com.fts.tenantbasededuportal.kafka;

public record UserCreatedTestEvent(Long userId, String username, String email) {}

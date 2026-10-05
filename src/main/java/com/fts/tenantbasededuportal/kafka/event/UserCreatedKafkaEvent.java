package com.fts.tenantbasededuportal.kafka.event;

import com.fts.tenantbasededuportal.socket.dto.UserCreatedSocketDto;

public record UserCreatedKafkaEvent(UserCreatedSocketDto user, String organizationId) {}
package com.fts.tenantbasededuportal.socket.dto;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketEventDto {

    private String id;
    private String eventType;
    private String payload;
    private Instant createdAt;
}

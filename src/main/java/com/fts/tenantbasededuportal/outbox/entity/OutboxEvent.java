package com.fts.tenantbasededuportal.outbox.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {

    @Id
    private String id;

    @Column(nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant publishedAt;

    public void markAsPublished() {
        this.publishedAt = Instant.now();
    }

    public static OutboxEvent create(
            final String eventType,
            final String payload) {

        return OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .eventType(eventType)
                .payload(payload)
                .createdAt(Instant.now())
                .build();
    }
}

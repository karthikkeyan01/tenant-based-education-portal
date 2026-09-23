package com.fts.tenantbasededuportal.socket.entity;

import com.fts.tenantbasededuportal.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "socket_event")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocketEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "recipient_user_id", nullable = false)
    private String recipientUserId;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    private Instant deliveredAt;
}

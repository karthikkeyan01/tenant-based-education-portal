package com.fts.tenantbasededuportal.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String action;

    private String entityAffected;

    private String entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String ipAddress;

    private String requestUrl;

    private String httpMethod;

}

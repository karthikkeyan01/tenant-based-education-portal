package com.fts.tenantbasededuportal.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;

    private String secondName;

    @ManyToOne(fetch = FetchType.LAZY)    //
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false)
    private Boolean active = false;

    private String activationToken;

    private Instant activationTokenExpiresAt;

    @Column(nullable = false)
    private Boolean mfaEnabled = false;

    private String otp;

    private Instant otpExpiresAt;

    private Instant lastLoginAt;
}

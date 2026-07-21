package com.fts.tenantbasededuportal.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private String password;

    private String firstName;
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;

    @JsonIgnore
    private String activationToken;
    @JsonIgnore
    private Instant activationTokenExpiresAt;

    @JsonIgnore
    private String resetPasswordToken;
    @JsonIgnore
    private Instant resetPasswordTokenExpiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Boolean mfaEnabled = false;
    @JsonIgnore
    private String otp;

    @JsonIgnore
    private Instant otpExpiresAt;
    private Instant lastLoginAt;
}

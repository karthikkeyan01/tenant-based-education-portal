package com.fts.tenantbasededuportal.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.Instant;

@Schema(description = "Authenticated user's profile details.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseDto {

    @Schema(description = "Unique identifier of the user.", example = "68b4f8f2d9a1c4e7b3f2a1d5")
    private String id;

    @Schema(description = "User's email address.", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's first name.", example = "John")
    private String firstName;

    @Schema(description = "User's last name.", example = "Doe")
    private String lastName;

    @Schema(description = "Name of the role assigned to the user.", example = "USER")
    private String roleName;

    @Schema(description = "Name of the organization the user belongs to.", example = "ABC University")
    private String organizationName;

    @Schema(description = "Indicates whether multi-factor authentication is enabled for the user.", example = "true")
    private Boolean mfaEnabled;

    @Schema(description = "Date and time when the user account was created.", example = "2026-07-17T14:30:15Z")
    private Instant createdAt;
}
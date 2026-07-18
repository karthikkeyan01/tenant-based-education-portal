package com.fts.tenantbasededuportal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Response returned after a login attempt.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {

    @Schema(description = "JWT access token used to authenticate subsequent requests.",
            example = "eyJhbGciOiJIUzI1NiJ9...")
    private String accessToken;

    @Schema(description = "Authenticated user's email address.", example = "john.doe@example.com")
    private String email;

    @Schema(description = "Role assigned to the authenticated user.", example = "USER")
    private String role;

    @Schema(description = "Indicates whether multi-factor authentication is required to complete the login process.",
            example = "true")
    private Boolean mfaRequired;

    @Schema(description = "Operation result message.", example = "Login successful.")
    private String message;
}

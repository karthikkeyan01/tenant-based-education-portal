package com.fts.tenantbasededuportal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "Request payload for user login.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequestDto {

    @Schema(description = "Registered email address.", example = "john.doe@example.com")
    @Email
    @NotBlank
    private String email;

    @Schema(description = "User password.", example = "Password@123")
    @NotBlank
    @Size(min = 8)
    private String password;
}

package com.fts.tenantbasededuportal.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "Request payload for user registration.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequestDto {

    @Schema(description = "Email address used to register the account.", example = "john.doe@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "Password for the new account.", example = "Password@123")
    @NotBlank
    @Size(min = 8)
    private String password;
}
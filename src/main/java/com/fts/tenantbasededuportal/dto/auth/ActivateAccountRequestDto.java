package com.fts.tenantbasededuportal.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ActivateAccountRequestDto {

    @NotBlank(message = "Activation token is required.")
    private String token;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank(message = "Confirm password is required.")
    private String confirmPassword;
}
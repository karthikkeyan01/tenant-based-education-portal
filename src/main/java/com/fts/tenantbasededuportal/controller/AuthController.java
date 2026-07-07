package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.auth.*;
import com.fts.tenantbasededuportal.service.AuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<Void>> register(
            @Valid
            @RequestBody
            final RegisterRequestDto request) {

        this.authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.<Void>builder()
                                .code(HttpStatus.CREATED.value())
                                .message("Registration successful.")
                                .build());
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(
            @Valid
            @RequestBody
            final LoginRequestDto request) {

        final LoginResponseDto response = this.authService.login(request);

        return ResponseEntity.ok(
                ApiResponseDto.<LoginResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("Login successful.")
                        .data(response)
                        .build());
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> verifyOtp(
            @RequestParam @Email final String email,
            @RequestParam @NotBlank final String otp){

        final LoginResponseDto response = this.authService.verifyOtp(email, otp);

        return ResponseEntity.ok(
                ApiResponseDto.<LoginResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("OTP verified successfully.")
                        .data(response)
                        .build());
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponseDto<Void>> resendOtp(
            @RequestParam @Email final String email) {

        this.authService.resendOtp(email);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("OTP resent successfully.")
                        .build());
    }

    @PostMapping("/activate-account")
    public ResponseEntity<ApiResponseDto<Void>> activateAccount(
            @RequestParam @NotBlank final String token,
            @RequestParam @NotBlank @Size(min = 8) final String password) {

        this.authService.activateAccount(token, password);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Account activated successfully.")
                        .build());
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto<Void>> forgotPassword(
            @RequestParam @Email final String email
    ) {

        this.authService.forgotPassword(email);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Password reset link sent successfully.")
                        .build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @RequestParam @NotBlank final String token,
            @RequestParam @NotBlank @Size(min = 8) final String password) {

        this.authService.resetPassword(token, password);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Password reset successfully.")
                        .build());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout() {

        this.authService.logout();

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Logout successful.")
                        .build());
    }
}

package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.auth.*;
import com.fts.tenantbasededuportal.service.AuthService;
import jakarta.validation.Valid;
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
                .body(new ApiResponseDto<>(
                        HttpStatus.CREATED.value(),
                        "Registration successful.",
                        null));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> login(
            @Valid
            @RequestBody
            final LoginRequestDto request) {

        final LoginResponseDto response = this.authService.login(request);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "Login successful.",
                        response));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponseDto<LoginResponseDto>> verifyOtp(
            @RequestParam final String email,
            @RequestParam final String otp){

        final LoginResponseDto response = this.authService.verifyOtp(email, otp);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "OTP verified successfully.",
                        response));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponseDto<Void>> resendOtp(
            @RequestParam final String email) {

        this.authService.resendOtp(email);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "OTP resent successfully.",
                        null));
    }

    @PostMapping("/activate-account")
    public ResponseEntity<ApiResponseDto<Void>> activateAccount(
            @RequestParam final String token,
            @RequestParam final String password) {

        this.authService.activateAccount(token, password);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "Account activated successfully.",
                        null));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponseDto<Void>> forgotPassword(
            @RequestParam final String email
    ) {

        this.authService.forgotPassword(email);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "Password reset link sent successfully.",
                        null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponseDto<Void>> resetPassword(
            @RequestParam final String token,
            @RequestParam final String password) {

        this.authService.resetPassword(token, password);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "Password reset successfully.",
                        null));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDto<Void>> logout() {

        this.authService.logout();

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "Logout successful.",
                        null));
    }

}

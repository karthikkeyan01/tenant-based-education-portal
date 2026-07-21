package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.auth.*;
import com.fts.tenantbasededuportal.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication", description = "APIs for Authentication and account management.")
@RestController
@RequestMapping("/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully."),
            @ApiResponse(responseCode = "409", description = "Email already exists.")})
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<Void> register(@Valid @RequestBody final RegisterRequestDto request) {

        this.authService.register(request);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.CREATED.value())
                .message("Registration successful.")
                .build();
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "Login successful."),
            @ApiResponse(responseCode = "401", description = "Invalid email or password."),
            @ApiResponse(responseCode = "500", description = "Failed to send the OTP email.")})
    @PostMapping("/login")
    public ApiResponseDto<LoginResponseDto> login(@Valid @RequestBody final LoginRequestDto request) {

        final LoginResponseDto response = this.authService.login(request);

        return ApiResponseDto.<LoginResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message(response.getMfaRequired()
                        ? "OTP sent to your email. Please verify MFA."
                        : "Login successful.")
                .data(response)
                .build();
    }

    @Operation(summary = "Verify MFA OTP",
            description = "Verifies the one-time password sent during multi-factor authentication via email and completes the login process.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "OTP verified successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP."),
            @ApiResponse(responseCode = "401", description = "Invalid email.")})
    @PostMapping("/verify-otp")
    public ApiResponseDto<LoginResponseDto> verifyOtp(@RequestParam @Email final String email,
                                                      @RequestParam @NotBlank final String otp){

        final LoginResponseDto response = this.authService.verifyOtp(email, otp);

        return ApiResponseDto.<LoginResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("OTP verified successfully.")
                .data(response)
                .build();
    }

    @Operation(summary = "Resend MFA OTP",
            description = "Generates and sends a new one-time password for multi-factor authentication via email.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "OTP resent successfully."),
            @ApiResponse(responseCode = "400", description = "MFA is not enabled for the user."),
            @ApiResponse(responseCode = "500", description = "Failed to send the OTP email.")})
    @PostMapping("/resend-otp")
    public ApiResponseDto<Void> resendOtp(@RequestParam @Email final String email) {

        this.authService.resendOtp(email);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("OTP resent successfully.")
                .build();
    }


    @Operation(summary = "Activate account", description = "Activates a user account using the activation token and sets the account password.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Account activated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid or expired activation token.")})
    @PostMapping("/activate-account")
    public ApiResponseDto<Void> activateAccount(@Parameter(description = "Account activation token received in the activation email.")
            @RequestParam @NotBlank final String token, @RequestParam @NotBlank @Size(min = 8) final String password) {

        this.authService.activateAccount(token, password);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Account activated successfully.")
                .build();
    }

    @Operation(summary = "Request password reset", description = "Generates a password reset token and sends a password reset email.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Password reset email sent successfully."),
            @ApiResponse(responseCode = "500", description = "Failed to send the password reset email.")})
    @PostMapping("/forgot-password")
    public ApiResponseDto<Void> forgotPassword(@RequestParam @Email final String email) {

        this.authService.forgotPassword(email);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Password reset link sent successfully.")
                .build();
    }

    @Operation(summary = "Reset password", description = "Resets the user's password using a valid password reset token.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Password reset successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid or expired password reset token.")})
    @PostMapping("/reset-password")
    public ApiResponseDto<Void> resetPassword(
            @Parameter(description = "Reset password token received in the email.") @RequestParam @NotBlank final String token,
            @RequestParam @NotBlank @Size(min = 8) final String password) {

        this.authService.resetPassword(token, password);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Password reset successfully.")
                .build();
    }

    @ApiResponse(responseCode = "200", description = "Logout successful.")
    @PostMapping("/logout")
    public ApiResponseDto<Void> logout() {

        this.authService.logout();

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Logout successful.")
                .build();
    }
}

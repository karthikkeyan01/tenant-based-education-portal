package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.auth.*;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.RolePermission;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.RolePermissionRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.security.JwtService;
import com.fts.tenantbasededuportal.security.UserPrincipal;
import com.fts.tenantbasededuportal.util.RoleConstants;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authenticationManager;

    private final JwtService jwtService;

    private final AuditService auditService;

    private final EmailService emailService;

    private final RolePermissionRepository  rolePermissionRepository;

    private final SecurityUtil  securityUtil;

    public void register(final RegisterRequestDto request){

        if(this.userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already exists");
        }

        final Role userRole = this.roleRepository.findByName(RoleConstants.USER)
                .orElseThrow(() -> new BadRequestException(
                        "Default User role not found"));

        final User user = User.builder()
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .deleted(false)
                .mfaEnabled(false)
                .otp(null)
                .otpExpiresAt(null)
                .lastLoginAt(null)
                .build();

        this.userRepository.save(user);

        this.auditService.log(
                user,
                "REGISTER",
                "USER",
                user.getId(),
                "User registered with email " + user.getEmail());
    }

    public LoginResponseDto login(final LoginRequestDto request){

        final Authentication authentication = this.authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        final UserPrincipal userPrincipal =
                (UserPrincipal) authentication.getPrincipal();

        if (userPrincipal == null) {
            throw new UnauthorizedException(
                    "Authenticated user not found");
        }

        final User user = userPrincipal.getUser();

        if (Boolean.TRUE.equals(user.getDeleted())) {

            throw new UnauthorizedException("User account is inactive.");
        }

        if (Boolean.TRUE.equals(user.getMfaEnabled())){

            String otp = String.valueOf(ThreadLocalRandom.current()
                    .nextInt(100000, 1000000));

            user.setOtp(this.passwordEncoder.encode(otp));

            user.setOtpExpiresAt(Instant.now().plusSeconds(300));

            this.userRepository.save(user);

            this.emailService.sendOtpMail(user.getEmail(), otp);

            this.auditService.log(
                    user,
                    "LOGIN_MFA_REQUIRED",
                    "USER",
                    user.getId(),
                    "MFA OTP sent during login");

            return LoginResponseDto.builder()
                    .email(user.getEmail())
                    .role(userPrincipal.getRoleName())
                    .mfaRequired(true)
                    .message("OTP sent to your email. Please verify MFA.")
                    .build();
        }

        final String token = this.jwtService.generateToken(userPrincipal);

        user.setLastLoginAt(Instant.now());

        this.userRepository.save(user);

        this.auditService.log(
                user,
                "LOGIN",
                "USER",
                user.getId(),
                "User logged in");

        return LoginResponseDto.builder()
                .accessToken(token)
                .email(userPrincipal.getUsername())
                .role(userPrincipal.getRoleName())
                .mfaRequired(false)
                .message("Login Successful.")
                .build();
    }

    public LoginResponseDto verifyMfa(final VerifyMfaRequestDto request){

        final User user = this.userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Email not found"));

        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {

            throw new BadRequestException("MFA is not enabled for this user.");
        }

        if (user.getOtp() == null || user.getOtpExpiresAt() == null) {

            throw new BadRequestException("No OTP request found");

        }

        if (Instant.now().isAfter(user.getOtpExpiresAt())) {

            user.setOtp(null);
            user.setOtpExpiresAt(null);

            this.userRepository.save(user);

            throw new BadRequestException("OTP expired");
        }

        if (!this.passwordEncoder.matches(request.getOtp(), user.getOtp())) {

            throw new BadRequestException("Invalid OTP");
        }

        final List<RolePermission> rolePermissions = this.rolePermissionRepository
                .findByRole(user.getRole());

        final UserPrincipal userPrincipal = new UserPrincipal(user, rolePermissions);

        final String token = this.jwtService.generateToken(userPrincipal);

        user.setOtp(null);

        user.setOtpExpiresAt(null);

        user.setLastLoginAt(Instant.now());

        this.userRepository.save(user);

        this.auditService.log(
                user,
                "LOGIN",
                "USER",
                user.getId(),
                "User logged in with MFA");

        return LoginResponseDto.builder()
                .accessToken(token)
                .email(user.getEmail())
                .role(user.getRole().getName())
                .mfaRequired(false)
                .message("MFA verification successful. User Logged in with MFA.")
                .build();
    }

    public void resendOtp(final ResendOtpRequestDto request) {

        final User user = this.userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException
                        ("Email not found"));

        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {

            throw new BadRequestException("MFA is not enabled.");
        }

        if (Boolean.TRUE.equals(user.getDeleted())) {

            throw new UnauthorizedException("User account is inactive.");
        }

        final String otp = String.valueOf(ThreadLocalRandom.current()
                                .nextInt(100000, 1000000));

        user.setOtp(this.passwordEncoder.encode(otp));

        user.setOtpExpiresAt(Instant.now().plusSeconds(300));

        this.userRepository.save(user);

        this.emailService.sendOtpMail(user.getEmail(), otp);

        this.auditService.log(
                user,
                "RESEND_OTP",
                "USER",
                user.getId(),
                "OTP resent for MFA verification");
    }

    public void logout(){

        final User currentUser = this.securityUtil.getCurrentUser();

        this.auditService.log(
                currentUser,
                "LOGOUT",
                "USER",
                currentUser.getId(),
                "User logged out");
    }
}

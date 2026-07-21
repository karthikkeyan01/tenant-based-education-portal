package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.auth.*;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ConflictException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.security.JwtService;
import com.fts.tenantbasededuportal.security.UserPrincipal;
import com.fts.tenantbasededuportal.util.constants.ApplicationConstants;
import com.fts.tenantbasededuportal.util.constants.AuditActionConstants;
import com.fts.tenantbasededuportal.util.constants.EntityAffectedConstants;
import com.fts.tenantbasededuportal.util.constants.RoleConstants;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final OtpGeneratorService otpGeneratorService;
    private final AuditService auditService;
    private final EmailService emailService;
    private final TokenGeneratorService tokenGeneratorService;
    private final SecurityUtil  securityUtil;

    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    public void register(final RegisterRequestDto request){

        if (this.userRepository.existsByEmail(request.getEmail())){
            throw new ConflictException("Email already exists");
        }

        final Role userRole = this.roleRepository.findByName(RoleConstants.USER).orElseThrow(() -> new IllegalStateException(
                        "User role not found"));
        final User user = User.builder()
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(request.getPassword()))
                .role(userRole)
                .organization(null)
                .active(true)
                .mfaEnabled(false)
                .otp(null)
                .otpExpiresAt(null)
                .activationToken(null)
                .activationTokenExpiresAt(null)
                .resetPasswordToken(null)
                .resetPasswordTokenExpiresAt(null)
                .lastLoginAt(null)
                .build();

        final User savedUser = this.userRepository.save(user);
        this.securityUtil.setAuthentication(savedUser);

        try{
            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.REGISTER)
                            .entityAffected(EntityAffectedConstants.AUTH)
                            .entityId(savedUser.getId())
                            .description("Individual user registered.")
                            .build());
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
    }

    public LoginResponseDto login(final LoginRequestDto request){

        final Authentication authentication = this.authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        final UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        final User user = this.userRepository.findByIdAndActiveTrue(userPrincipal.getId()).orElseThrow(() ->
                new IllegalStateException("User not found."));

        if (Boolean.TRUE.equals(user.getMfaEnabled())){
            final String otp = this.otpGeneratorService.generateOtp();

            user.setOtp(this.passwordEncoder.encode(otp));
            user.setOtpExpiresAt(Instant.now().plus(ApplicationConstants.OTP_EXPIRY_MINUTES,ChronoUnit.MINUTES));

            this.userRepository.save(user);
            this.emailService.sendOtpMail(user.getEmail(), otp);

            try{
                this.auditService.create(
                        AuditRequestDto.builder()
                                .action(AuditActionConstants.LOGIN)
                                .entityAffected(EntityAffectedConstants.AUTH)
                                .entityId(user.getId())
                                .description("OTP sent for login.")
                                .build());

                return LoginResponseDto.builder()
                        .email(user.getEmail())
                        .role(userPrincipal.getRole())
                        .mfaRequired(true)
                        .build();
            }
            finally {
                this.securityUtil.clearAuthentication();
            }
        }
        user.setLastLoginAt(Instant.now());
        this.userRepository.save(user);

        try{

            this.auditService.create(AuditRequestDto.builder()
                    .action(AuditActionConstants.LOGIN)
                    .entityAffected(EntityAffectedConstants.AUTH)
                    .entityId(user.getId())
                    .description("User logged in.")
                    .build());
            final String token = this.jwtService.generateToken(userPrincipal);

            return LoginResponseDto.builder()
                    .accessToken(token)
                    .email(userPrincipal.getUsername())
                    .role(userPrincipal.getRole())
                    .mfaRequired(false)
                    .build();
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
    }

    @Transactional
    public LoginResponseDto verifyOtp(final String email, final String otp){

        final User user = this.userRepository.findByEmailAndActiveTrue(email).orElseThrow(() -> new UnauthorizedException(
                        "Invalid email or OTP."));

        if (!Boolean.TRUE.equals(user.getMfaEnabled())) {
            throw new BadRequestException("MFA is not enabled for this user.");
        }
        if (user.getOtp() == null || user.getOtpExpiresAt() == null) {
            throw new BadRequestException("No OTP has been generated.");
        }
        if (Instant.now().isAfter(user.getOtpExpiresAt())) {
            user.setOtp(null);
            user.setOtpExpiresAt(null);
            this.userRepository.save(user);

            throw new BadRequestException("OTP has expired");
        }

        if (!this.passwordEncoder.matches(otp, user.getOtp())) {
            throw new BadRequestException("Invalid OTP");
        }

        user.setOtp(null);
        user.setOtpExpiresAt(null);
        user.setLastLoginAt(Instant.now());

        final User savedUser = this.userRepository.save(user);
        final UserPrincipal principal = new UserPrincipal(savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getPassword(),
                savedUser.getOrganization() != null
                        ? savedUser.getOrganization().getId()
                        : null,
                savedUser.getRole().getName(),
                savedUser.getActive());
        this.securityUtil.setAuthentication(savedUser);

        try{
            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.LOGIN)
                            .entityAffected(EntityAffectedConstants.AUTH)
                            .entityId(savedUser.getId())
                            .description("User logged in using MFA.")
                            .build());
            final String token = this.jwtService.generateToken(principal);

            return LoginResponseDto.builder()
                    .accessToken(token)
                    .email(savedUser.getEmail())
                    .role(savedUser.getRole().getName())
                    .mfaRequired(false)
                    .build();
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
    }

    @Transactional
    public void resendOtp(final String email) {

        final User user = this.userRepository.findByEmailAndActiveTrue(email).orElse(null);

        if (user == null) {
            return;
        }
        if (!user.getMfaEnabled()) {
            throw new BadRequestException("MFA is not enabled.");
        }

        final String otp = this.otpGeneratorService.generateOtp();
        user.setOtp(this.passwordEncoder.encode(otp));
        user.setOtpExpiresAt(Instant.now()
                .plus(ApplicationConstants.OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));

        this.userRepository.save(user);
        this.emailService.sendOtpMail(user.getEmail(), otp);
        this.securityUtil.setAuthentication(user);

        try{
            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.RESEND_OTP)
                            .entityAffected(EntityAffectedConstants.AUTH)
                            .entityId(user.getId())
                            .description("OTP resent for MFA verification.")
                            .build());
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
    }

    @Transactional
    public void activateAccount(final String token, final String password) {

        final User user = this.userRepository.findByActivationToken(token).orElseThrow(() -> new BadRequestException("Invalid token."));

        if (user.getActive()) {
            throw new BadRequestException(
                    "Account is already activated.");
        }
        if (user.getActivationTokenExpiresAt() == null || Instant.now().isAfter(user.getActivationTokenExpiresAt())) {
            throw new BadRequestException("Activation link has expired.");
        }

        user.setPassword(this.passwordEncoder.encode(password));
        user.setActive(true);
        user.setActivationToken(null);
        user.setActivationTokenExpiresAt(null);
        final User savedUser = this.userRepository.save(user);
        this.securityUtil.setAuthentication(savedUser);

        try{
            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.ACTIVATE_ACCOUNT)
                            .entityAffected(EntityAffectedConstants.AUTH)
                            .entityId(savedUser.getId())
                            .description("User activated account using activation link.")
                            .build());
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
    }

    @Transactional
    public void forgotPassword(final String email) {

        final User user = this.userRepository.findByEmailAndActiveTrue(email).orElse(null);

        if (user == null) {
            return;
        }
        final String resetToken = this.tokenGeneratorService.generateToken();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiresAt(Instant.now()
                .plus(ApplicationConstants.RESET_PASSWORD_EXPIRY_MINUTES
                        , ChronoUnit.MINUTES));

        final User savedUser = this.userRepository.save(user);
        final String resetLink = this.baseUrl + "/auth/reset-password?token=" + resetToken;

        this.emailService.sendForgotPasswordMail(user.getEmail(), resetLink);
        this.securityUtil.setAuthentication(savedUser);

        try{
            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.FORGOT_PASSWORD)
                            .entityAffected(EntityAffectedConstants.AUTH)
                            .entityId(savedUser.getId())
                            .description("Password reset link sent.")
                            .build());
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
    }

    @Transactional
    public void resetPassword(final String token, final String password) {

        final User user = this.userRepository.findByResetPasswordToken(token).orElseThrow(() -> new BadRequestException(
                        "Invalid token."));

        if (user.getResetPasswordTokenExpiresAt() == null || Instant.now().isAfter(user.getResetPasswordTokenExpiresAt())) {
            throw new BadRequestException("Reset password link has expired.");
        }

        user.setPassword(this.passwordEncoder.encode(password));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiresAt(null);
        final User savedUser = this.userRepository.save(user);
        this.securityUtil.setAuthentication(savedUser);

        try{
            this.auditService.create(
                    AuditRequestDto.builder()
                            .action(AuditActionConstants.RESET_PASSWORD)
                            .entityAffected(EntityAffectedConstants.AUTH)
                            .entityId(savedUser.getId())
                            .description("User reset password.")
                            .build());
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
    }

    @Transactional
    public void logout() {

        this.auditService.create(AuditRequestDto.builder()
                        .action(AuditActionConstants.LOGOUT)
                        .entityAffected(EntityAffectedConstants.AUTH)
                        .entityId(this.securityUtil.getCurrentUserId())
                        .description("User logged out.")
                        .build());
    }
}

package com.fts.tenantbasededuportal.startup;

import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.service.EmailService;
import com.fts.tenantbasededuportal.service.TokenGeneratorService;
import com.fts.tenantbasededuportal.util.constants.ApplicationConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorService tokenGeneratorService;
    private final EmailService emailService;
    @Value("${app.super-admin.email}")
    private String superAdminEmail;
    @Value("${app.super-admin.temporary-password}")
    private String temporaryPassword;
    @Value("${app.base-url}")
    private String baseUrl;

    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void initializeSuperAdmin() {

        log.info("Initializing Super Admin");
        final User user = this.userRepository.findByEmailAndActiveTrue(this.superAdminEmail).orElseThrow(() -> new
                IllegalStateException("Active Super Admin not found."));

        if (user.getResetPasswordToken() != null) {
            log.info("Super admin reset password token already exists. Skipping email.");
            return;
        }
        if (!this.passwordEncoder.matches(this.temporaryPassword, user.getPassword())) {
            log.info("Super admin password has already been changed. Skipping email.");
            return;
        }

        final String resetToken = this.tokenGeneratorService.generateToken();
        user.setResetPasswordToken(resetToken);
        user.setResetPasswordTokenExpiresAt(Instant.now().plus(ApplicationConstants.RESET_PASSWORD_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        this.userRepository.save(user);

        final String resetLink = this.baseUrl + "/auth/reset-password?token=" + resetToken;
        this.emailService.sendSuperAdminCredentialsMail(user.getEmail(), this.temporaryPassword, resetLink);
        log.info("Super Admin credentials email sent successfully to '{}'.", user.getEmail());

    }
}

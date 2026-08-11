package com.fts.tenantbasededuportal.security.oauth;

import tools.jackson.databind.JsonNode;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import tools.jackson.databind.ObjectMapper;
import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.auth.LoginResponseDto;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.ErrorResponse;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.security.CustomUserDetailsService;
import com.fts.tenantbasededuportal.security.JwtService;
import com.fts.tenantbasededuportal.security.UserPrincipal;
import com.fts.tenantbasededuportal.service.AuditService;
import com.fts.tenantbasededuportal.service.EmailService;
import com.fts.tenantbasededuportal.service.OtpGeneratorService;
import com.fts.tenantbasededuportal.service.PasswordGeneratorService;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import com.fts.tenantbasededuportal.util.constants.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Optional;


@Component
@Slf4j
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordGeneratorService passwordGeneratorService;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final OtpGeneratorService  otpGeneratorService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final SecurityUtil securityUtil;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final WebClient webClient;

    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
                                        final Authentication authentication) throws IOException, ServletException {

        final OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
        final OAuth2User oauth2User = oauth2Token.getPrincipal();
        final String provider = oauth2Token.getAuthorizedClientRegistrationId().toUpperCase(Locale.ROOT);
        log.info("{} OAuth2 authentication processing started.", provider);

        String email;
        String firstName;
        String lastName;

        if (AuthProviderConstants.GOOGLE.equals(provider)) {
            email = oauth2User.getAttribute("email");
            firstName = oauth2User.getAttribute("given_name");
            lastName = oauth2User.getAttribute("family_name");

        } else if (AuthProviderConstants.GITHUB.equals(provider)) {
            email = oauth2User.getAttribute("email");
            final String name = oauth2User.getAttribute("name");
            firstName = null;
            lastName = null;

            if (name != null && !name.isBlank()) {
                final String[] nameParts = name.trim().split("\\s+", 2);
                firstName = nameParts[0];
                if (nameParts.length > 1) {
                    lastName = nameParts[1];
                }
            }
            if (email == null || email.isBlank()) {
                email = this.getGithubEmail(oauth2Token, authentication.getName());
            }
        } else {
            this.writeErrorResponse(request, response, HttpStatus.BAD_REQUEST, "Unsupported OAuth2 provider.");
            return;
        }

        if (email == null || email.isBlank()) {
            this.writeErrorResponse(request, response, HttpStatus.UNAUTHORIZED, "OAuth2 provider did not provide an email address.");
            return;
        }
        log.info("{} authentication succeeded for email '{}'.", provider, email);

        final Optional<User> optionalUser = this.userRepository.findByEmail(email);
        final User user;

        if (optionalUser.isPresent()) {
            user = optionalUser.get();
            if (AuthProviderConstants.LOCAL.equals(user.getAuthProvider())) {
                this.writeErrorResponse(request, response, HttpStatus.UNAUTHORIZED, "This account is registered using email and password. " + "Please sign in using your password.");
                return;
            }
            if (!provider.equalsIgnoreCase(user.getAuthProvider())) {
                final String providerName = AuthProviderConstants.GOOGLE.equals(user.getAuthProvider()) ? "Google" : "GitHub";
                this.writeErrorResponse(request, response, HttpStatus.UNAUTHORIZED, "This account is registered using " + providerName + ". Please continue using " + providerName + ".");
                return;
            }
        }
        else {
            final Role userRole;
            try {
                userRole = this.roleRepository.findByName(RoleConstants.USER).orElseThrow(() ->
                                new IllegalStateException("User role not found"));
            }
            catch (final IllegalStateException exception) {
                log.error("Required USER role was not found while creating OAuth2 user.", exception);
                this.writeErrorResponse(request, response, HttpStatus.INTERNAL_SERVER_ERROR, "Unable to create your account at this time.");
                return;
            }

            final String generatedPassword = this.passwordGeneratorService.generatePassword(ApplicationConstants.GENERATED_PASSWORD_LENGTH);

            final String encodedPassword = this.passwordEncoder.encode(generatedPassword);

            user = User.builder()
                    .email(email)
                    .password(encodedPassword)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(userRole)
                    .organization(null)
                    .active(true)
                    .authProvider(provider)
                    .mfaEnabled(false)
                    .lastLoginAt(Instant.now())
                    .build();
            this.userRepository.save(user);
        }

        final UserPrincipal userPrincipal = this.customUserDetailsService.loadUserById(user.getId());

        if (Boolean.TRUE.equals(user.getMfaEnabled())) {

            final String otp = this.otpGeneratorService.generateOtp();
            user.setOtp(this.passwordEncoder.encode(otp));
            user.setOtpExpiresAt(Instant.now().plus(ApplicationConstants.OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));
            this.userRepository.save(user);
            this.emailService.sendOtpMail(user.getEmail(), otp);

            this.securityUtil.setAuthentication(user);

            try {
                this.auditService.create(AuditRequestDto.builder()
                        .action(AuditActionConstants.LOGIN)
                        .entityAffected(EntityAffectedConstants.AUTH)
                        .entityId(user.getId())
                        .description("OTP sent for login verification.")
                        .build());
                log.info("OTP generated and sent to '{}' for login verification.", user.getEmail());
            }
            finally {
                this.securityUtil.clearAuthentication();
            }

            final LoginResponseDto loginResponse = LoginResponseDto.builder()
                    .email(user.getEmail())
                    .role(user.getRole().getName())
                    .mfaRequired(true)
                    .build();
            final ApiResponseDto<LoginResponseDto> apiResponse = ApiResponseDto.<LoginResponseDto>builder()
                    .code(HttpStatus.OK.value())
                    .message("OTP sent successfully.")
                    .data(loginResponse)
                    .build();
            response.setStatus(HttpStatus.OK.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            this.objectMapper.writeValue(response.getWriter(), apiResponse);
            return;
        }

        user.setLastLoginAt(Instant.now());
        this.userRepository.save(user);

        this.securityUtil.setAuthentication(user);
        try {
            this.auditService.create(AuditRequestDto.builder()
                    .action(AuditActionConstants.LOGIN)
                    .entityAffected(EntityAffectedConstants.AUTH)
                    .entityId(user.getId())
                    .description("User logged in using " + provider + ".")
                    .build());
        }
        finally {
            this.securityUtil.clearAuthentication();
        }
        final String token = this.jwtService.generateToken(userPrincipal);
        final LoginResponseDto loginResponse = LoginResponseDto.builder()
                .accessToken(token)
                .email(user.getEmail())
                .role(user.getRole().getName())
                .mfaRequired(false)
                .build();
        final ApiResponseDto<LoginResponseDto> apiResponse = ApiResponseDto.<LoginResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("User logged in successfully!")
                .data(loginResponse)
                .build();
        response.setStatus(HttpStatus.OK.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        this.objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    private void writeErrorResponse(final HttpServletRequest request, final HttpServletResponse response,
                                    final HttpStatus status, final String message) throws IOException {

        final ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        this.objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    private String getGithubEmail(final OAuth2AuthenticationToken oauth2Token, final String principalName) {

        final OAuth2AuthorizedClient authorizedClient = this.authorizedClientService.loadAuthorizedClient(oauth2Token.getAuthorizedClientRegistrationId(), principalName);
        if (authorizedClient == null) {
            log.warn("GitHub authorized client was not found.");
            return null;
        }

        final String accessToken = authorizedClient.getAccessToken().getTokenValue();
        final JsonNode emails = this.webClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(headers -> {
                    headers.setBearerAuth(accessToken);
                    headers.set("Accept", "application/vnd.github+json");
                })
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (emails == null || !emails.isArray()) {
            log.warn("GitHub email API returned no email information.");
            return null;
        }

        for (JsonNode emailNode : emails) {
            final boolean primary = emailNode.path("primary").asBoolean(false);
            final boolean verified = emailNode.path("verified").asBoolean(false);
            if (primary && verified) {
                return emailNode.path("email").asString(null);
            }
        }
        for (JsonNode emailNode : emails) {
            final boolean verified = emailNode.path("verified").asBoolean(false);
            if (verified) {
                return emailNode.path("email").asString(null);
            }
        }
        log.warn("GitHub did not provide a primary or verified email address.");
        return null;
    }
}

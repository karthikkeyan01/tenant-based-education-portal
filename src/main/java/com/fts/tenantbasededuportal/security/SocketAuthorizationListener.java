package com.fts.tenantbasededuportal.security;

import com.socketio4j.socketio.AuthorizationListener;
import com.socketio4j.socketio.AuthorizationResult;
import com.socketio4j.socketio.HandshakeData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SocketAuthorizationListener implements AuthorizationListener {

    private final JwtService jwtService;
    private final CustomUserDetailsService customUserDetailsService;

    @Override
    public AuthorizationResult getAuthorizationResult(final HandshakeData handshakeData) {

        try {
            final String token = handshakeData.getSingleUrlParam("token");
            if (token == null || token.isEmpty()) {
                log.warn("Socket.IO connection rejected: JWT token is missing");
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            final String userId = this.jwtService.extractUserId(token);
            final UserPrincipal principal = this.customUserDetailsService.loadUserById(userId);

            if (!this.jwtService.validateToken(token, principal)) {
                log.warn("Socket.IO connection rejected: invalid JWT token for user '{}'.", userId);
                return AuthorizationResult.FAILED_AUTHORIZATION;
            }

            log.info("Socket.IO connection successful for user '{}'.", principal.getEmail());
            final Map<String, Object> storeParams = new HashMap<>();
            storeParams.put("authenticatedUser", principal);

            return new AuthorizationResult(true, storeParams);
        }
        catch (final JwtException | IllegalArgumentException |
                     org.springframework.security.core.userdetails.UsernameNotFoundException |
                     com.fts.tenantbasededuportal.exception.AccountInactiveException |
                     com.fts.tenantbasededuportal.exception.OrganizationInactiveException exception) {

            log.warn("Socket.IO authentication failed: {}", exception.getMessage());
            return AuthorizationResult.FAILED_AUTHORIZATION;
        }
    }
}

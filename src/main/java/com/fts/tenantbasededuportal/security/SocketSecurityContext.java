package com.fts.tenantbasededuportal.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SocketSecurityContext {

    public void execute(final com.socketio4j.socketio.SocketIOClient client, final Runnable action) {
        final UserPrincipal principal = client.get("authenticatedUser");
        if (principal == null) {
            throw new IllegalStateException("AAuthenticated user not found for socket.");
        }

        final UsernamePasswordAuthenticationToken authentication =  new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {
            action.run();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}

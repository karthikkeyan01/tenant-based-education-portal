package com.fts.tenantbasededuportal.socket;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fts.tenantbasededuportal.security.SocketAuthorizationListener;
import com.fts.tenantbasededuportal.security.UserPrincipal;
import com.fts.tenantbasededuportal.socket.registry.SocketUserRegistry;
import com.fts.tenantbasededuportal.socket.service.SocketEventService;
import com.socketio4j.socketio.SocketIOServer;
import com.socketio4j.socketio.protocol.JacksonJsonSupport;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@org.springframework.context.annotation.Configuration
@RequiredArgsConstructor
@Slf4j
public class SocketIOConfig {

    private final SocketIOServer server;
    private final SocketAuthorizationListener socketAuthorizationListener;
    private final SocketUserRegistry socketUserRegistry;
    private final SocketEventService socketEventService;

    @PostConstruct
    public void configure() {

        this.server.getConfiguration().setJsonSupport(new JacksonJsonSupport(new JavaTimeModule()));

        this.server.getConfiguration().setAuthorizationListener(this.socketAuthorizationListener);

        this.server.addConnectListener(client -> {
            final UserPrincipal principal = client.get("authenticatedUser");
            this.socketUserRegistry.add(principal.getId(), client);
            log.info("SOCKET CONNECTED - user='{}', email='{}', socket='{}'", principal.getId(), principal.getEmail(), client.getSessionId());
            if (principal.getOrganizationId() != null) {
                final String organizationRoom = "organization:" + principal.getOrganizationId();
                client.joinRoom(organizationRoom);
                log.info("User '{}' joined room '{}'", principal.getEmail(), organizationRoom);
                this.socketEventService.deliverPendingEvents(principal.getId());
            }
            log.info("Client connected: {}", client.getSessionId());
        });

        server.addDisconnectListener(client -> {
            final String sessionId = client.getSessionId().toString();
            this.socketUserRegistry.remove(client);
            log.info("SOCKET DISCONNECTED - socket='{}'", sessionId);
        });
    }
}

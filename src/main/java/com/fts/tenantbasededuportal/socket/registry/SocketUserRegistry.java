package com.fts.tenantbasededuportal.socket.registry;

import com.socketio4j.socketio.SocketIOClient;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SocketUserRegistry {

    private final Map<String, Set<SocketIOClient>> connectedUsers = new ConcurrentHashMap<>();
    private final Map<String, String> socketUsers = new ConcurrentHashMap<>();

    public void add(final String userId, final SocketIOClient client) {
        this.connectedUsers.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet()).add(client);
        final String sessionId = client.getSessionId().toString();
        this.socketUsers.put(sessionId, userId);
    }

    public void remove(final SocketIOClient client) {
        final String sessionId = client.getSessionId().toString();
        final String userId = this.socketUsers.remove(sessionId);
        if (userId == null) {
            return;
        }

        final Set<SocketIOClient> clients = this.connectedUsers.get(userId);
        if (clients == null) {
            return;
        }

        clients.remove(client);
        if (clients.isEmpty()) {
            this.connectedUsers.remove(userId);
        }
    }

    public Set<SocketIOClient> get(final String userId) {
        final Set<SocketIOClient> clients = this.connectedUsers.get(userId);
        if (clients == null) {
            return Set.of();
        }
        return Set.copyOf(clients);
    }
}

package com.fts.tenantbasededuportal.socket.service;

import com.fts.tenantbasededuportal.socket.entity.SocketEvent;
import com.fts.tenantbasededuportal.socket.registry.SocketUserRegistry;
import com.fts.tenantbasededuportal.socket.repository.SocketEventRepository;
import com.socketio4j.socketio.AckCallback;
import com.socketio4j.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocketEventDeliveryService {

    private final SocketEventRepository socketEventRepository;
    private final SocketUserRegistry socketUserRegistry;

    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public void deliverEvents(final List<SocketEvent> socketEvents) {

        for (final SocketEvent socketEvent : socketEvents) {

            final Set<SocketIOClient> clients = this.socketUserRegistry.get(socketEvent.getRecipientUserId());
            log.info("Delivering event '{}' to user '{}'. Connected sockets: {}", socketEvent.getId(), socketEvent.getRecipientUserId(), clients.size());
            if (clients.isEmpty()) {
                continue;
            }

            for (final SocketIOClient client : clients) {
                try {
                    log.info("Sending USER_CREATED event '{}' to socket '{}'", socketEvent.getId(), client.getSessionId());
                    client.sendEvent("user-created", new AckCallback<Object>(Object.class) {
                        @Override
                        public void onSuccess(final Object result) {
                            log.info("ACK received for event '{}' from socket '{}'", socketEvent.getId(), client.getSessionId());
                            socketEvent.setDeliveredAt(Instant.now());
                            socketEventRepository.save(socketEvent);
                        }
                        @Override
                        public void onTimeout() {
                            log.warn("ACK timeout for event '{}' from socket '{}'", socketEvent.getId(), client.getSessionId());
                        }
                    },
                    socketEvent.getPayload());
                }
                catch (Exception exception) {
                    log.error("Failed to deliver socket event '{}' to user '{}'.", socketEvent.getId(),
                            socketEvent.getRecipientUserId(), exception);
                }
            }
        }
    }
}

package com.fts.tenantbasededuportal.socket.service;

import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.event.UserCreatedEvent;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.socket.entity.SocketEvent;
import com.fts.tenantbasededuportal.socket.repository.SocketEventRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocketEventService {

    private final UserRepository userRepository;
    private final SocketEventRepository socketEventRepository;
    private final ObjectMapper objectMapper;
    private final SocketEventDeliveryService socketEventDeliveryService;

    @Transactional(propagation =  Propagation.REQUIRES_NEW)
    public List<SocketEvent> createUserCreatedSocketEvents(final UserCreatedEvent event) {

        final List<User> users = this.userRepository.findByOrganization_Id(event.organizationId());
        final String payload = this.objectMapper.writeValueAsString(event.user());
        final List<SocketEvent> socketEvents = new ArrayList<>();

        for (final User user : users) {

            final SocketEvent socketEvent = SocketEvent.builder()
                    .recipientUserId(user.getId())
                    .eventType("USER_CREATED")
                    .payload(payload)
                    .build();
            socketEvents.add(this.socketEventRepository.save(socketEvent));
        }
        log.info("Created persistent user-created socket events for {} users in organization '{}'.", users.size(), event.organizationId());

        return socketEvents;
    }

    public void deliverPendingEvents(final String userId) {

        final List<SocketEvent> pendingEvents = this.socketEventRepository.findByRecipientUserIdAndDeliveredAtIsNullOrderByCreatedAtAsc(userId);
        if (pendingEvents.isEmpty()) {
            return;
        }
        log.info("Found {} pending socket events for user '{}'.", pendingEvents.size(), userId);

        this.socketEventDeliveryService.deliverEvents(pendingEvents);
    }
}

package com.fts.tenantbasededuportal.kafka.consumer;

import com.fts.tenantbasededuportal.kafka.event.UserCreatedKafkaEvent;
import com.fts.tenantbasededuportal.socket.entity.SocketEvent;
import com.fts.tenantbasededuportal.event.UserCreatedEvent;
import com.fts.tenantbasededuportal.socket.service.SocketEventDeliveryService;
import com.fts.tenantbasededuportal.socket.service.SocketEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCreatedKafkaConsumer {

    private final SocketEventService socketEventService;
    private final SocketEventDeliveryService socketEventDeliveryService;

    @KafkaListener(topics = "user-created", groupId = "user-created-socket-group")
    public void consume(final UserCreatedKafkaEvent event) {
        final List<SocketEvent> socketEvents = this.socketEventService.createUserCreatedSocketEvents(
                new UserCreatedEvent(event.user(), event.organizationId()));
        this.socketEventDeliveryService.deliverEvents(socketEvents);
    }
}

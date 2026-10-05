package com.fts.tenantbasededuportal.outbox.service;

import com.fts.tenantbasededuportal.kafka.event.UserCreatedKafkaEvent;
import com.fts.tenantbasededuportal.outbox.entity.OutboxEvent;
import com.fts.tenantbasededuportal.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, UserCreatedKafkaEvent> kafkaTemplate;

    private final Set<String> publishingEvents = ConcurrentHashMap.newKeySet();

    @Scheduled(fixedRate = 5000)
    public void publishEvent() {

        final List<OutboxEvent> events = this.outboxEventRepository.findByPublishedAtIsNullOrderByCreatedAtAsc();

        for (final OutboxEvent event : events) {

            if (!this.publishingEvents.add(event.getId())) {
                continue;
            }
            try {
                final UserCreatedKafkaEvent kafkaEvent = this.objectMapper.readValue(event.getPayload(), UserCreatedKafkaEvent.class);
                this.kafkaTemplate.send("user-created", String.valueOf(kafkaEvent.user().id()), kafkaEvent).thenAccept(result -> {
                    event.markAsPublished();
                    this.outboxEventRepository.save(event);
                    this.publishingEvents.remove(event.getId());
                }).exceptionally(error -> {
                    log.error("Failed to publish outbox event: {}", event.getId(), error);
                    this.publishingEvents.remove(event.getId());
                    return null;
                });
            }
            catch (final Exception e) {
                log.error("Failed to process outbox event: {}", event.getId(), e);
                this.publishingEvents.remove(event.getId());
            }
        }
    }
}

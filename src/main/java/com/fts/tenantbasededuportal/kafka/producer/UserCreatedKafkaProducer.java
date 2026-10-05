package com.fts.tenantbasededuportal.kafka.producer;

import com.fts.tenantbasededuportal.kafka.event.UserCreatedKafkaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCreatedKafkaProducer {

    private static final String TOPIC = "user-created";

    private final KafkaTemplate<String, UserCreatedKafkaEvent> kafkaTemplate;

    public void publish(final UserCreatedKafkaEvent event) {
        this.kafkaTemplate.send(TOPIC, String.valueOf(event.user().id()), event);
    }
}

package com.fts.tenantbasededuportal.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaTestProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTemplate<String, UserCreatedTestEvent> userCreatedKafkaTemplate;

    public void sendMessage(final String message) {
        kafkaTemplate.send("test-topic", message);
    }

    public void sendConcurrencyMessage(final int partition, final String message) {
        kafkaTemplate.send("concurrency-test", partition, null, message);
    }

    public void sendUserCreated(final UserCreatedTestEvent event) {
        userCreatedKafkaTemplate.send("user-created-test", event);
    }
}

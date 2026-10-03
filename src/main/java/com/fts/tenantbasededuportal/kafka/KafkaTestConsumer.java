package com.fts.tenantbasededuportal.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaTestConsumer {

    // Normal consumer
    @KafkaListener(topics = "test-topic", groupId = "earliest-clean-test")
    public void consumeFromBeginning(final String message) {
        System.out.println("Received from Kafka" + message);
    }

    @KafkaListener(topics = "test-topic", groupId = "spring-test-group")
    public void consumeNormally(final String message) {
        System.out.println("Normal: " + message);
    }

    // Concurrency experiment
    @KafkaListener(topics = "concurrency-test", groupId = "concurrency-test-group", concurrency = "3")
    public void consumeWithConcurrency(final String message, final ConsumerRecord<String, String> record) {
        System.out.println(Thread.currentThread().getName() + " -> Partition: " + record.partition()
                        + " -> Offset: " + record.offset() + " -> Message: " + message);
    }

    @KafkaListener(topics = "user-created-test", groupId = "user-created-test-group")
    public void consumeUserCreated(final UserCreatedTestEvent event) {
        System.out.println("Received UserCreatedTestEvent:");
        System.out.println("User ID: " + event.userId());
        System.out.println("Username: " + event.username());
        System.out.println("Email: " + event.email());
    }
}

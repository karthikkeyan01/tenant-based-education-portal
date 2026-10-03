package com.fts.tenantbasededuportal.kafka;

import com.fts.tenantbasededuportal.util.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Kafka Management", description = "Send requests to Kafka producer.")
@SecurityRequirement(name = SwaggerConstants.SECURITY_SCHEME_NAME)
@RestController
@RequiredArgsConstructor
public class KafkaTestController {

    private final KafkaTestProducer kafkaTestProducer;

    @PostMapping("/kafka/test")
    public String sendMessage(@RequestParam final String message) {
        kafkaTestProducer.sendMessage(message);
        return "Message sent to Kafka: " + message;
    }

    @PostMapping("/kafka/concurrency-test")
    public String sendConcurrencyMessage(@RequestParam final String message, @RequestParam final int partition) {
        kafkaTestProducer.sendConcurrencyMessage(partition, message);
        return "Message sent to concurrency-test";
    }

    @PostMapping("/kafka/user-created-test")
    public String sendUserCreated() {
        final UserCreatedTestEvent event = new UserCreatedTestEvent(100L, "karthik", "karthik@test.com");
        kafkaTestProducer.sendUserCreated(event);
        return "UserCreatedTestEvent sent";
    }
}

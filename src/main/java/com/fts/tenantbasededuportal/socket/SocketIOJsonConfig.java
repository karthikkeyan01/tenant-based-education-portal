package com.fts.tenantbasededuportal.socket;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.socketio4j.socketio.protocol.JacksonJsonSupport;
import com.socketio4j.socketio.protocol.JsonSupport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SocketIOJsonConfig {

    @Bean
    public JsonSupport jsonSupport() {
        return new JacksonJsonSupport(new JavaTimeModule());
    }
}
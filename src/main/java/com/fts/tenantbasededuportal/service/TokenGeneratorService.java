package com.fts.tenantbasededuportal.service;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenGeneratorService {

    public String generateToken() {

        return UUID.randomUUID().toString().replace("-", "");
    }
}

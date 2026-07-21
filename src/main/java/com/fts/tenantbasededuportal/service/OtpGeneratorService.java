package com.fts.tenantbasededuportal.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpGeneratorService {

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateOtp() {
        return String.format("%06d", this.secureRandom.nextInt(1_000_000));
    }
}

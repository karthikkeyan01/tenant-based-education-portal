package com.fts.tenantbasededuportal.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class PasswordGeneratorService {

    public static final String CHARACTERS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" +
                    "0123456789" + "!@#$%^&*";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generatePassword(final int length) {

        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {

            password.append(CHARACTERS.charAt
                    (secureRandom.nextInt(CHARACTERS.length())));
        }

        return password.toString();
    }
}

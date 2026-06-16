package com.fts.tenantbasededuportal.util;

import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {

    private final UserRepository userRepository;

    public User getCurrentUser() {

        final String email = this.getCurrentUserEmail();

        return this.userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException(
                                "Authenticated user not found."));
    }

    public String getCurrentUserEmail() {

        final Authentication authentication =
                SecurityContextHolder.getContext()
                        .getAuthentication();

        if (authentication == null) {

            throw new UnauthorizedException(
                    "Authenticated user not found.");
        }

        return authentication.getName();
    }
}
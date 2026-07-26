package com.fts.tenantbasededuportal.security;

import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.AccountInactiveException;
import com.fts.tenantbasededuportal.exception.OrganizationInactiveException;
import com.fts.tenantbasededuportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserPrincipal loadUserByUsername(final String email){

        final User user = this.userRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("Authentication failed because user '{}' was not found.", email);
            return new UsernameNotFoundException("User not found: " + email);});

        if (!user.getActive()) {
            log.warn("Authentication failed because user '{}' is inactive.", user.getEmail());
            throw new AccountInactiveException("User account is inactive");
        }

        if (user.getOrganization() != null && !user.getOrganization().getActive()){
            log.warn("Authentication failed because organization '{}' is inactive for user '{}'.",
                    user.getOrganization().getName(), user.getEmail());
            throw new OrganizationInactiveException("Organization is Inactive");
        }

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getOrganization() != null
                        ? user.getOrganization().getId()
                        : null,
                user.getRole().getName(),
                user.getActive());
    }

    public UserPrincipal loadUserById(final String id){

        final User user = this.userRepository.findById(id).orElseThrow(() -> {
            log.warn("JWT authentication failed because user ID '{}' was not found.", id);
            return new UsernameNotFoundException("User not found: " + id);});

        if (!user.getActive()) {
            log.warn("JWT authentication failed because user '{}' is inactive.", user.getEmail());
            throw new AccountInactiveException("User account is inactive");
        }

        if (user.getOrganization() != null && !user.getOrganization().getActive()){
            log.warn("JWT authentication failed because organization '{}' is inactive for user '{}'.",
                    user.getOrganization().getName(), user.getEmail());
            throw new OrganizationInactiveException("Organization is inactive");
        }

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                user.getOrganization() != null
                        ? user.getOrganization().getId()
                        : null,
                user.getRole().getName(),
                user.getActive());
    }
}
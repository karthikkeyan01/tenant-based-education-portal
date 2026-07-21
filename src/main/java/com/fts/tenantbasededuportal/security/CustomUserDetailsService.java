package com.fts.tenantbasededuportal.security;

import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.AccountInactiveException;
import com.fts.tenantbasededuportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserPrincipal loadUserByUsername(final String email){

        final User user = this.userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException(
                "User not found : " + email));

        if (!user.getActive()) {
            throw new AccountInactiveException("User account is inactive");
        }

        if (user.getOrganization() != null && !user.getOrganization().getActive()){
            throw new AccountInactiveException("Organization is Inactive");
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

        final User user = this.userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException(
                "User not found : " + id));

        if (!user.getActive()) {
            throw new AccountInactiveException("User account is inactive");
        }

        if (user.getOrganization() != null && !user.getOrganization().getActive()){
            throw new AccountInactiveException("Organization is inactive");
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
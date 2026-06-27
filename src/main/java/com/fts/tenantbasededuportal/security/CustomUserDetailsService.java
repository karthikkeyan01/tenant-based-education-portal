package com.fts.tenantbasededuportal.security;

import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.repository.RolePermissionRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public UserDetails loadUserByUsername(final String email){

        final User user = this.userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                        "User not found : " + email));

        if (Boolean.TRUE.equals(user.getDeleted())) {

            throw new UsernameNotFoundException("User account is inactive");
        }

        if (user.getOrganization() != null && Boolean.TRUE.equals(
                user.getOrganization().getDeleted())){

            throw new UsernameNotFoundException("Your organization is Inactive");
        }

        return new UserPrincipal(user, user.getRole());
    }
}
package com.fts.tenantbasededuportal.security;

import com.fts.tenantbasededuportal.entity.RolePermission;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.repository.RolePermissionRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    private final RolePermissionRepository rolePermissionRepository;

    @Override
    public UserDetails loadUserByUsername(final String email){

        final User user = this.userRepository.findByEmail(email)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                        "User not found : " + email));

        final List<RolePermission> rolePermissions =
                this.rolePermissionRepository
                        .findByRole(user.getRole());

        return new UserPrincipal(user, rolePermissions);
    }
}
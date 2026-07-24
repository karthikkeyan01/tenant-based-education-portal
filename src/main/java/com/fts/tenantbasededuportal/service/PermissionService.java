package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.entity.RolePermission;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.repository.RolePermissionRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;
    private final SecurityUtil securityUtil;

    public void requirePermission(final String permission) {

        final User currentUser = this.securityUtil.getCurrentUser();
        if(!this.hasPermission(currentUser, permission)){
            log.warn("Permission '{}' denied for user '{}' (ID: {}).", permission, currentUser.getEmail(), currentUser.getId());
            throw new AccessDeniedException("You do not have permission to perform this action.");
        }
    }

    private boolean hasPermission(final User user, final String permission) {

        final List<RolePermission> rolePermissions = this.rolePermissionRepository.findByRole(user.getRole());
        for (final RolePermission rolePermission : rolePermissions) {
            if (rolePermission.getPermission().getName().equals(permission)) {
                return true;
            }
        }
        return false;
    }
}

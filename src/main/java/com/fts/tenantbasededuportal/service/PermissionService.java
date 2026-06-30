package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.entity.RolePermission;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.enums.PermissionType;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.RolePermissionRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final RolePermissionRepository rolePermissionRepository;

    private final SecurityUtil securityUtil;

    public void requirePermission(final PermissionType permission) {

        final User currentUser = this.securityUtil.getCurrentUser();

        if(!this.hasPermission(currentUser, permission)){

            throw new UnauthorizedException
                    ("You do not have permission to perform this action");
        }
    }

    private boolean hasPermission(final User user,
                                  final PermissionType permission) {

        final List<RolePermission> rolePermissions =
                this.rolePermissionRepository.findByRole(user.getRole());

        for (final RolePermission rolePermission : rolePermissions) {

            if (rolePermission.getPermission()
                    .getName()
                    .equals(permission.name())) {

                return true;
            }
        }

        return false;
    }
}

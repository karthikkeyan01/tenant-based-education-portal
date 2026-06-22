package com.fts.tenantbasededuportal.initializer;

import com.fts.tenantbasededuportal.entity.Permission;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.RolePermission;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.repository.PermissionRepository;
import com.fts.tenantbasededuportal.repository.RolePermissionRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @Value("${app.super-admin.email}")
    private String SUPER_ADMIN_EMAIL;

    @Value("${app.super-admin.password}")
    private String SUPER_ADMIN_PASSWORD;

    private final RoleRepository roleRepository;

    private final PermissionRepository permissionRepository;

    private final RolePermissionRepository rolePermissionRepository;

    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args){
        this.initializeRoles();
        this.initializePermissions();
        this.initializeRolePermissions();
        this.initializeSuperAdmin();
    }

    private void initializeRoles(){

        final String[] roles = {
                "SUPER_ADMIN",
                "ORG_ADMIN",
                "USER"
        };

        for(final String roleName : roles){

            if(!this.roleRepository.existsByName(roleName)){

                this.roleRepository.save(Role.builder()
                                .name(roleName)
                                .build()
                );
            }
        }
    }

    private void initializePermissions(){

        final String[] permissions = {
                "VIEW_USERS",
                "CREATE_USER",
                "UPDATE_USER",
                "DELETE_USER",
                "MANAGE_USER",
                "VIEW_PROFILE",
                "UPDATE_PROFILE",
                "CHANGE_PASSWORD",
                "CREATE_ORGANIZATION",
                "VIEW_ORGANIZATIONS",
                "UPDATE_ORGANIZATION",
                "DELETE_ORGANIZATION",
                "MANAGE_SYSTEM"
        };

        for(final String permissionName : permissions){

            if (!this.permissionRepository.existsByName(permissionName)){

                this.permissionRepository.save(Permission.builder()
                                .name(permissionName)
                                .build()
                );
            }
        }
    }

    private void initializeRolePermissions(){

        final Role superAdmin = this.roleRepository
                        .findByName("SUPER_ADMIN")
                        .orElseThrow();

        final Role orgAdmin = this.roleRepository
                        .findByName("ORG_ADMIN")
                        .orElseThrow();

        final Role user = this.roleRepository
                        .findByName("USER")
                        .orElseThrow();

        this.assignAllPermissions(superAdmin);

        this.assignPermissions(
                orgAdmin,
                "VIEW_USERS",
                "CREATE_USER",
                "UPDATE_USER",
                "MANAGE_USER",
                "DELETE_USER",
                "VIEW_PROFILE",
                "UPDATE_PROFILE",
                "CHANGE_PASSWORD",
                "VIEW_ORGANIZATIONS"
        );

        this.assignPermissions(
                user,
                "VIEW_PROFILE",
                "UPDATE_PROFILE",
                "CHANGE_PASSWORD"
        );
    }

    private void assignAllPermissions(final Role role){

        final List<Permission> permissions = this.permissionRepository.findAll();

        for (final Permission permission : permissions){
            this.assignPermission(role, permission.getName());
        }
    }

    private void assignPermissions(
            final Role role, final String... permissions){

        for (final String permission : permissions){
            this.assignPermission(role, permission);
        }
    }

    private void assignPermission(
            final Role role, final String permissionName){

        final Permission permission = this.permissionRepository.findByName(
                permissionName).orElseThrow();

        final List<RolePermission> rolePermissions =
                this.rolePermissionRepository.findByRole(role);

        boolean exists = false;

        for(final RolePermission rolePermission : rolePermissions){

            if (rolePermission.getPermission()
                    .getId()
                    .equals(permission.getId())){

                exists = true;
                break;
            }
        }

        if(!exists){
            final RolePermission rolePermission = RolePermission.builder()
                    .role(role)
                    .permission(permission)
                    .build();

            this.rolePermissionRepository.save(rolePermission);
        }
    }

    private void initializeSuperAdmin(){

        if (this.userRepository.existsByEmail(SUPER_ADMIN_EMAIL)){
            return;
        }

        final  Role superAdminRole = this.roleRepository
                .findByName("SUPER_ADMIN").orElseThrow();

        final String password = this.passwordEncoder
                .encode(SUPER_ADMIN_PASSWORD);

        this.userRepository.save(User.builder()
                .email(SUPER_ADMIN_EMAIL)
                .password(password)
                .deleted(false)
                .role(superAdminRole)
                .mfaEnabled(false)
                .build()
        );
    }
}

package com.fts.tenantbasededuportal.initializer;

import com.fts.tenantbasededuportal.entity.Permission;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.RolePermission;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.EmailDeliveryException;
import com.fts.tenantbasededuportal.repository.PermissionRepository;
import com.fts.tenantbasededuportal.repository.RolePermissionRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.service.EmailService;
import com.fts.tenantbasededuportal.service.PasswordGeneratorService;
import com.fts.tenantbasededuportal.service.TokenGeneratorService;
import com.fts.tenantbasededuportal.util.constants.ApplicationConstants;
import com.fts.tenantbasededuportal.util.constants.PermissionConstants;
import com.fts.tenantbasededuportal.util.constants.RoleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserRepository userRepository;
    private final PasswordGeneratorService passwordGeneratorService;
    private final TokenGeneratorService tokenGeneratorService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Transactional
    public void run(final String... args){
        log.info("Database initialization started.");
        this.initializeRoles();
        this.initializePermissions();
        this.initializeRolePermissions();
        this.initializeSuperAdmin();
        log.info("Database initialization completed successfully.");
    }

    private void initializeRoles(){

        log.info("Initializing default roles.");
        final String[] roles = {
                RoleConstants.SUPER_ADMIN,
                RoleConstants.ORG_ADMIN,
                RoleConstants.USER
        };

        for(final String roleName : roles){
            if(!this.roleRepository.existsByName(roleName)){
                this.roleRepository.save(Role.builder()
                                .name(roleName)
                                .build());
            }
        }
    }

    private void initializePermissions(){

        log.info("Initializing default permissions.");
        final String[] permissions = {
                PermissionConstants.VIEW_USERS,
                PermissionConstants.CREATE_USER,
                PermissionConstants.MANAGE_USER,
                PermissionConstants.RESEND_ACTIVATION_EMAIL,
                PermissionConstants.VIEW_PROFILE,
                PermissionConstants.UPDATE_PROFILE,
                PermissionConstants.CHANGE_PASSWORD,
                PermissionConstants.CREATE_ORGANIZATION,
                PermissionConstants.VIEW_ORGANIZATIONS,
                PermissionConstants.UPDATE_ORGANIZATION,
                PermissionConstants.MANAGE_ORGANIZATION,
                PermissionConstants.VIEW_AUDIT_LOGS,
                PermissionConstants.MANAGE_SYSTEM
        };

        for(final String permissionName : permissions){
            if (!this.permissionRepository.existsByName(permissionName)){
                this.permissionRepository.save(Permission.builder()
                                .name(permissionName)
                                .build());
            }
        }
    }

    private void initializeRolePermissions(){

        log.info("Initializing role permissions.");
        final Role superAdmin = this.roleRepository.findByName(RoleConstants.SUPER_ADMIN).orElseThrow();
        final Role orgAdmin = this.roleRepository.findByName(RoleConstants.ORG_ADMIN).orElseThrow();
        final Role user = this.roleRepository.findByName(RoleConstants.USER).orElseThrow();

        this.assignAllPermissions(superAdmin);

        this.assignPermissions(
                orgAdmin,
                PermissionConstants.VIEW_USERS,
                PermissionConstants.CREATE_USER,
                PermissionConstants.MANAGE_USER,
                PermissionConstants.RESEND_ACTIVATION_EMAIL,
                PermissionConstants.VIEW_PROFILE,
                PermissionConstants.UPDATE_PROFILE,
                PermissionConstants.CHANGE_PASSWORD);

        this.assignPermissions(
                user,
                PermissionConstants.VIEW_PROFILE,
                PermissionConstants.UPDATE_PROFILE,
                PermissionConstants.CHANGE_PASSWORD);
    }

    private void assignAllPermissions(final Role role){
        final List<Permission> permissions = this.permissionRepository.findAll();
        for (final Permission permission : permissions){
            this.assignPermission(role, permission.getName());
        }
    }

    private void assignPermissions(final Role role, final String... permissions){
        for (final String permission : permissions){
            this.assignPermission(role, permission);
        }
    }

    private void assignPermission(final Role role, final String permissionName){

        final Permission permission = this.permissionRepository.findByName(permissionName).orElseThrow();
        final List<RolePermission> rolePermissions = this.rolePermissionRepository.findByRole(role);

        boolean exists = false;

        for(final RolePermission rolePermission : rolePermissions){
            if (rolePermission.getPermission().getId().equals(permission.getId())){
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

        if (this.userRepository.existsByEmail(ApplicationConstants.SUPER_ADMIN_EMAIL)){
            log.info("Super Admin already exists. Skipping initialization.");
            return;
        }

        log.info("Creating Super Admin account.");
        final  Role superAdminRole = this.roleRepository.findByName(RoleConstants.SUPER_ADMIN).orElseThrow(() ->
                        new IllegalStateException("Super_Admin role not found."));
        final String generatedPassword = this.passwordGeneratorService.generatePassword(ApplicationConstants.GENERATED_PASSWORD_LENGTH);
        final String encodedPassword = passwordEncoder.encode(generatedPassword);
        final String resetPasswordToken = this.tokenGeneratorService.generateToken();

        final User superAdmin = User.builder()
                .email(ApplicationConstants.SUPER_ADMIN_EMAIL)
                .password(encodedPassword)
                .active(true)
                .firstName("Super")
                .lastName("Admin")
                .role(superAdminRole)
                .resetPasswordToken(resetPasswordToken)
                .resetPasswordTokenExpiresAt(Instant.now()
                        .plus(ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS
                                ,ChronoUnit.HOURS))
                .mfaEnabled(false)
                .organization(null)
                .build();

        final User savedUser = this.userRepository.save(superAdmin);
        final String resetPasswordLink = this.baseUrl + "/auth/reset-password?token=" + resetPasswordToken;

        try{
            this.emailService.sendSuperAdminCredentialsMail(savedUser.getEmail(), generatedPassword, resetPasswordLink);
            log.info("Super Admin account initialized successfully.");
        }
        catch (final EmailDeliveryException exception) {
            log.error("Failed to send Super Admin credentials email.", exception);
            throw new EmailDeliveryException("Failed to send Super Admin credentials email.");
        }

    }
}

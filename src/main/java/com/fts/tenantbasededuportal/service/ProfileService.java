package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.profile.ChangePasswordRequestDto;
import com.fts.tenantbasededuportal.dto.profile.ProfileResponseDto;
import com.fts.tenantbasededuportal.dto.profile.UpdateProfileRequestDto;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import com.fts.tenantbasededuportal.util.constants.AuditActionConstants;
import com.fts.tenantbasededuportal.util.constants.EntityAffectedConstants;
import com.fts.tenantbasededuportal.util.constants.PermissionConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    private final SecurityUtil securityUtil;

    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;

    private final PermissionService permissionService;

    //performs a GET request and fetches the profile of logged-in user.
    @Transactional(readOnly = true)
    public ProfileResponseDto retrieveProfile() {
        this.permissionService.requirePermission(PermissionConstants.VIEW_PROFILE);

        final User currentUser = this.securityUtil.getCurrentUser();

        return ProfileResponseDto.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .firstName(currentUser.getFirstName())
                .lastName(currentUser.getLastName())
                .roleName(currentUser.getRole().getName())
                .organizationName(currentUser.getOrganization() != null
                        ? currentUser.getOrganization().getName()
                        : null)
                .mfaEnabled(currentUser.getMfaEnabled())
                .createdAt(currentUser.getCreatedAt())
                .build();
    }

    //performs a PUT request and updates profile of logged-in user.
    @Transactional
    public ProfileResponseDto updateProfile
            (final UpdateProfileRequestDto request) {

        this.permissionService.requirePermission(PermissionConstants.UPDATE_PROFILE);

        final User currentUser = this.securityUtil.getCurrentUser();

        if (request.getFirstName() != null){

            final String firstName = request.getFirstName().trim();

            if (firstName.isBlank()){

                throw new BadRequestException("First name cannot be blank.");
            }

            currentUser.setFirstName(firstName);
        }

        if (request.getLastName() != null) {

            final String lastName = request.getLastName().trim();

            if (lastName.isBlank()){

                throw new BadRequestException("Last name cannot be blank.");
            }

            currentUser.setLastName(lastName);
        }

        if (request.getMfaEnabled() != null) {

            currentUser.setMfaEnabled(request.getMfaEnabled());
        }

        String auditDetails = "Updated user profile.";

        if (request.getMfaEnabled() != null) {

            if(request.getMfaEnabled()){
                auditDetails +=  " and enabled MFA.";
            }
            else {
                auditDetails +=  " and disabled MFA.";
            }
        }

        final User savedUser = this.userRepository.save(currentUser);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action(AuditActionConstants.UPDATE_PROFILE)
                        .entityAffected(EntityAffectedConstants.USER)
                        .entityId(currentUser.getId())
                        .description(auditDetails)
                        .build()
        );

        return ProfileResponseDto.builder()
                .id(savedUser.getId())
                .email(savedUser.getEmail())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .roleName(savedUser.getRole().getName())
                .organizationName(savedUser.getOrganization() != null
                        ? savedUser.getOrganization().getName()
                        : null)
                .mfaEnabled(savedUser.getMfaEnabled())
                .createdAt(savedUser.getCreatedAt())
                .build();
    }

    //performs a PUT request and changes the password of logged-in user.
    //note needs old password and new password (must match)
    @Transactional
    public void changeProfilePassword
            (final ChangePasswordRequestDto request) {

        this.permissionService.requirePermission(PermissionConstants.CHANGE_PASSWORD);

        final User currentUser = this.securityUtil.getCurrentUser();

        if (!this.passwordEncoder.matches
                (request.getOldPassword(), currentUser.getPassword())) {

            throw new BadRequestException(
                    "Old password is incorrect.");
        }

        if (request.getNewPassword() == null) {

            throw new BadRequestException("New password is required.");
        }

        final String newPassword = request.getNewPassword().trim();

        if (newPassword.isBlank()) {

            throw new BadRequestException("New password is required.");
        }

        if(request.getOldPassword().equals(newPassword)) {

            throw new BadRequestException(
                    "New password must be different from old password.");
        }

        currentUser.setPassword(
                this.passwordEncoder.encode(newPassword));

        this.userRepository.save(currentUser);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action(AuditActionConstants.CHANGE_PASSWORD)
                        .entityAffected(EntityAffectedConstants.USER)
                        .entityId(currentUser.getId())
                        .description("User changed their password.")
                        .build());
    }
}

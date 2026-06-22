package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.profile.ChangePasswordRequestDto;
import com.fts.tenantbasededuportal.dtos.profile.ProfileResponseDto;
import com.fts.tenantbasededuportal.dtos.profile.UpdateProfileRequestDto;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;

    private final SecurityUtil securityUtil;

    private final PasswordEncoder passwordEncoder;

    private final AuditService auditService;

    public ProfileResponseDto fetchProfile() {

        final User currentUser = this.securityUtil.getCurrentUser();

        String organizationName = null;

        if (currentUser.getOrganization() != null) {
            organizationName = currentUser.getOrganization().getName();
        }

        this.auditService.log(
                currentUser,
                "VIEW_PROFILE",
                "USER",
                currentUser.getId(),
                "Viewed own profile");

        return ProfileResponseDto.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .firstName(currentUser.getFirstName())
                .secondName(currentUser.getSecondName())
                .roleName(currentUser.getRole().getName())
                .organizationName(organizationName)
                .mfaEnabled(currentUser.getMfaEnabled())
                .build();
    }

    public ProfileResponseDto updateProfile
            (final UpdateProfileRequestDto request) {

        final User currentUser = this.securityUtil.getCurrentUser();

        if (request.getFirstName() != null) {
            currentUser.setFirstName(request.getFirstName());
        }

        if (request.getSecondName() != null) {
            currentUser.setSecondName(request.getSecondName());
        }

        if (request.getMfaEnabled() != null) {
            currentUser.setMfaEnabled(request.getMfaEnabled());
        }

        String details = "Updated own profile";

        if (request.getMfaEnabled() != null) {

            if(request.getMfaEnabled()){
                details +=  " and enabled MFA";
            }
            else {
                details +=  " and disabled MFA";
            }
        }

        this.userRepository.save(currentUser);

        String organizationName = null;

        if (currentUser.getOrganization() != null) {
            organizationName = currentUser.getOrganization().getName();
        }

        this.auditService.log(
                currentUser,
                "UPDATE_PROFILE",
                "USER",
                currentUser.getId(),
                details);

        return ProfileResponseDto.builder()
                .id(currentUser.getId())
                .email(currentUser.getEmail())
                .firstName(currentUser.getFirstName())
                .secondName(currentUser.getSecondName())
                .roleName(currentUser.getRole().getName())
                .organizationName(organizationName)
                .mfaEnabled(currentUser.getMfaEnabled())
                .build();
    }

    public void changeProfilePassword
            (final ChangePasswordRequestDto request) {

        final User currentUser = this.securityUtil.getCurrentUser();

        if (!this.passwordEncoder.matches
                (request.getOldPassword(), currentUser.getPassword())) {

            throw new BadRequestException(
                    "Old password is incorrect");
        }

        if (request.getNewPassword() == null
                || request.getNewPassword().isBlank()) {

            throw new BadRequestException("New password is required");
        }

        if(request.getOldPassword().equals(request.getNewPassword())) {

            throw new BadRequestException(
                    "New password must be different from old password");
        }

        currentUser.setPassword(
                this.passwordEncoder.encode(request.getNewPassword()));

        this.userRepository.save(currentUser);

        this.auditService.log(
                currentUser,
                "CHANGE_PASSWORD",
                "USER",
                currentUser.getId(),
                "Changed password");
    }
}

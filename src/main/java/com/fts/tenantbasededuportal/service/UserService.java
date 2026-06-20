package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.user.CreateUserRequestDto;
import com.fts.tenantbasededuportal.dtos.user.UpdateUserRequestDto;
import com.fts.tenantbasededuportal.dtos.user.UserResponseDto;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.fts.tenantbasededuportal.util.RoleConstants;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final SecurityUtil securityUtil;

    private final RoleRepository roleRepository;

    private final OrganizationRepository organizationRepository;

    private final PasswordEncoder passwordEncoder;

    public List<UserResponseDto> fetchUsers(){

        final User currentUser = this.securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        final List<User> users;

        if(RoleConstants.SUPER_ADMIN.equals(roleName)){

            users = this.userRepository.findAll();
        }
        else if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            users = this.userRepository.findByOrganization(
                    currentUser.getOrganization());
        }
        else {
            throw new UnauthorizedException(
                    "you don't have permission to view users");
        }

        final List<UserResponseDto> response = new ArrayList<>();

        for(final User user : users){

            String organizationName = null;

            if (user.getOrganization() != null){

                organizationName = user.getOrganization().getName();
            }

            response.add(
                    UserResponseDto.builder()
                            .id(user.getId())
                            .email(user.getEmail())
                            .roleName(user.getRole().getName())
                            .mfaEnabled(user.getMfaEnabled())
                            .deleted(user.getDeleted())
                            .organizationName(organizationName)
                            .createdAt(user.getCreatedAt())
                            .build()
            );
        }

        return response;
    }

    public UserResponseDto fetchUserById(final String id){

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String roleName = currentUser.getRole().getName();

        if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization().getId()
                    .equals(currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot access users from another organization."
                );
            }
        }

        String organizationName = null;

        if (targetUser.getOrganization() != null) {

            organizationName = targetUser.getOrganization().getName();
        }

        return UserResponseDto.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .roleName(targetUser.getRole().getName())
                .organizationName(organizationName)
                .mfaEnabled(targetUser.getMfaEnabled())
                .deleted(targetUser.getDeleted())
                .createdAt(targetUser.getCreatedAt())
                .build();
    }

    public UserResponseDto createUser(final CreateUserRequestDto request){

        final User currentUser = this.securityUtil.getCurrentUser();

        if(this.userRepository.existsByEmail(request.getEmail())){
            throw new BadRequestException("Email already exists");
        }

        final String currentRole = currentUser.getRole().getName();

        final String requestedRole = request.getRoleName();

        final Role role;

        final Organization organization;

        if(RoleConstants.SUPER_ADMIN.equals(currentRole)){

            if(!RoleConstants.USER.equals(requestedRole)
            && !RoleConstants.ORG_ADMIN.equals(requestedRole)){

                throw new BadRequestException("Invalid role.");
            }

            if (RoleConstants.ORG_ADMIN.equals(requestedRole)
                    && request.getOrganizationId() == null) {

                throw new BadRequestException(
                        "Organization admin must belong to an organization.");
            }

            role = this.roleRepository.findByName(requestedRole)
                    .orElseThrow(()->new ResourceNotFoundException(
                            "Role not found"));

            if (request.getOrganizationId() != null){

                organization = this.organizationRepository
                        .findById(request.getOrganizationId())
                        .orElseThrow(()->new ResourceNotFoundException(
                                "Organization not found"));
            }
            else {
                organization = null;
            }
        }
        else if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            if(!RoleConstants.USER.equals(requestedRole)){

                throw new UnauthorizedException(
                        "Organization admins can only create users");
            }

            role = this.roleRepository.findByName(RoleConstants.USER)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Role not found"));

            organization = currentUser.getOrganization();
        }
        else {

            throw new UnauthorizedException(
                    "You do not have permission to create users.");
        }

        final User user = User.builder()
                .email(request.getEmail())
                .password(this.passwordEncoder.encode(
                        request.getPassword()))
                .firstName(request.getFirstName())
                .secondName(request.getSecondName())
                .role(role)
                .organization(organization)
                .deleted(false)
                .mfaEnabled(false)
                .build();

        this.userRepository.save(user);

        String organizationName = null;

        if (organization != null){

            organizationName = organization.getName();

        }

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .roleName(role.getName())
                .organizationName(organizationName)
                .mfaEnabled(user.getMfaEnabled())
                .deleted(user.getDeleted())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public UserResponseDto updateUser(final String id,
            final UpdateUserRequestDto request) {

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String currentRole = currentUser.getRole().getName();

        if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization()
                    .getId()
                    .equals(currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot update users from another organization."
                );
            }
        }

        if (request.getEmail() != null
                && !RoleConstants.SUPER_ADMIN.equals(currentRole)) {

            throw new UnauthorizedException(
                    "Only super admins can update email."
            );
        }

        if (RoleConstants.SUPER_ADMIN.equals(currentRole)
                && request.getEmail() != null) {

            if (!targetUser.getEmail()
                    .equals(request.getEmail())
                    && this.userRepository.existsByEmail(
                    request.getEmail())) {

                throw new BadRequestException(
                        "Email already exists."
                );
            }

            targetUser.setEmail(
                    request.getEmail()
            );
        }

        if (request.getFirstName() != null) {
            targetUser.setFirstName(request.getFirstName());
        }

        if (request.getSecondName() != null) {
            targetUser.setSecondName(request.getSecondName());
        }

        if (RoleConstants.SUPER_ADMIN.equals(currentRole)
                && request.getRoleName() != null) {

            if (!RoleConstants.USER.equals(request.getRoleName())
                    && !RoleConstants.ORG_ADMIN.equals(request.getRoleName())) {

                throw new BadRequestException("Invalid role.");
            }

            if (RoleConstants.ORG_ADMIN.equals(request.getRoleName())
                    && request.getOrganizationId() == null
                    && targetUser.getOrganization() == null) {

                throw new BadRequestException(
                        "Organization admin must belong to an organization."
                );
            }

            final Role role = this.roleRepository.findByName(
                                    request.getRoleName())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Role not found."));

            targetUser.setRole(role);
        }

        if (RoleConstants.SUPER_ADMIN.equals(currentRole)
                && request.getOrganizationId() != null) {

            final Organization organization = this.organizationRepository
                            .findById(request.getOrganizationId())
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Organization not found."));

            targetUser.setOrganization(organization);
        }

        this.userRepository.save(targetUser);

        String organizationName = null;

        if (targetUser.getOrganization() != null) {

            organizationName = targetUser.getOrganization().getName();
        }

        return UserResponseDto.builder()
                .id(targetUser.getId())
                .email(targetUser.getEmail())
                .firstName(targetUser.getFirstName())
                .secondName(targetUser.getSecondName())
                .roleName(targetUser.getRole().getName())
                .organizationName(organizationName)
                .mfaEnabled(targetUser.getMfaEnabled())
                .deleted(targetUser.getDeleted())
                .createdAt(targetUser.getCreatedAt())
                .build();
    }

    public void deleteUser(final String id) {

        final User currentUser = this.securityUtil.getCurrentUser();

        final User targetUser = this.userRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                        "User not found."));

        final String currentRole = currentUser.getRole().getName();

        if (RoleConstants.ORG_ADMIN.equals(currentRole)) {

            if (targetUser.getOrganization() == null
                    || !targetUser.getOrganization().getId().equals(
                            currentUser.getOrganization().getId())) {

                throw new UnauthorizedException(
                        "You cannot delete users from another organization."
                );
            }
        }

        if (Boolean.TRUE.equals(targetUser.getDeleted())) {

            throw new BadRequestException(
                    "User is already deleted."
            );
        }

        targetUser.setDeleted(true);

        this.userRepository.save(targetUser);
    }
}

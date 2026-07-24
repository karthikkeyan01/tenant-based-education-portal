package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.organization.*;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ConflictException;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.constants.*;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AuditService auditService;
    private final EmailService emailService;
    private final PasswordGeneratorService passwordGeneratorService;
    private final PasswordEncoder passwordEncoder;
    private final TokenGeneratorService tokenGeneratorService;
    private final PermissionService permissionService;

    @Value("${app.base-url}")
    private String baseUrl;

    //performs a POST operation and creates an organization.
    //only super admin can create organizations
    @Transactional
    public CreateOrganizationResponseDto createOrganization(final CreateOrganizationRequestDto request) {

        log.info("Organization creation requested.");
        if (!this.securityUtil.isSuperAdmin()){
            throw new UnauthorizedException("You are not allowed to perform this action");
        }
        this.permissionService.requirePermission(PermissionConstants.CREATE_ORGANIZATION);

        if (this.organizationRepository.existsByName(request.getOrganizationName())) {
            log.warn("Organization creation failed because organization '{}' already exists.", request.getOrganizationName());
            throw new ConflictException("Organization already exists.");
        }
        if (this.userRepository.existsByEmail(request.getOrgAdminEmail())){
            log.warn("Organization creation failed because user '{}' already exists.", request.getOrgAdminEmail());
            throw new ConflictException("User already exists.");
        }

        final Role orgAdminRole = this.roleRepository.findByName(RoleConstants.ORG_ADMIN).orElseThrow(() ->
                                new IllegalStateException("Role Not Found."));
        final Organization organization = Organization.builder().name(request.getOrganizationName()).active(true).build();
        final Organization savedOrganization = this.organizationRepository.save(organization);
        final NewOrganizationAdminData data = NewOrganizationAdminData.builder()
                .email(request.getOrgAdminEmail())
                .firstName(request.getOrgAdminFirstName())
                .lastName(request.getOrgAdminLastName())
                .role(orgAdminRole)
                .organization(savedOrganization)
                .build();
        final User savedUser = this.createInactiveOrganizationAdmin(data);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action(AuditActionConstants.CREATE_ORGANIZATION)
                        .entityAffected(EntityAffectedConstants.ORGANIZATION)
                        .entityId(savedOrganization.getId())
                        .description("Created organization: "
                                + savedOrganization.getName()
                                + " along with it's admin")
                        .build());
        log.info("Organization '{}' created successfully with administrator '{}'.", savedOrganization.getName(), savedUser.getEmail());

        return CreateOrganizationResponseDto.builder()
                .organizationId(savedOrganization.getId())
                .organizationName(savedOrganization.getName())
                .orgActiveStatus(savedOrganization.getActive())
                .orgAdminEmail(savedUser.getEmail())
                .orgAdminFirstName(savedUser.getFirstName())
                .orgAdminLastName(savedUser.getLastName())
                .orgAdminActiveStatus(savedUser.getActive())
                .createdAt(savedOrganization.getCreatedAt())
                .build();
    }

    //performs a GET operation and fetches all organizations.
    //can be performed only by super admin.
    @Transactional(readOnly = true)
    public Page<OrganizationResponseDto> retrieveAllOrganizations(final int page, final int size) {

        log.info("Organization retrieval requested.");
        if (!this.securityUtil.isSuperAdmin()){
            throw new UnauthorizedException("Only super admin can view organizations");
        }
        this.permissionService.requirePermission(PermissionConstants.VIEW_ORGANIZATIONS);

        final Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<Organization> organizations = this.organizationRepository.findByActiveTrue(pageable);

        log.info("Organizations retrieved successfully.");
        return organizations.map( organization ->
                OrganizationResponseDto.builder()
                        .id(organization.getId())
                        .name(organization.getName())
                        .active(organization.getActive())
                        .createdAt(organization.getCreatedAt())
                        .build());
    }

    //performs a GET operation and fetches organization based on org id.
    //can be accessed by super admin.
    @Transactional(readOnly = true)
    public OrganizationResponseDto retrieveOrganizationById(final String id) {

        log.info("Organization retrieval requested for organization ID: '{}'.",id);
        if (!this.securityUtil.isSuperAdmin()){
            throw new UnauthorizedException("Only super admin can view organization");
        }
        this.permissionService.requirePermission(PermissionConstants.VIEW_ORGANIZATIONS);

        final Organization organization = this.organizationRepository.findByIdAndActiveTrue(id).orElseThrow(() -> {
            log.warn("Organization retrieval failed because organization ID '{}' was not found.", id);
            return new ResourceNotFoundException("Organization not found");});

        log.info("Organization '{}' retrieved successfully.", organization.getId());

        return OrganizationResponseDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .active(organization.getActive())
                .createdAt(organization.getCreatedAt())
                .build();
    }

    //performs a PUT operation and updates organization.
    //can be only performed by super admin.
    @Transactional
    public OrganizationResponseDto updateOrganizationById(final String id, final OrganizationRequestDto request) {

        log.info("Organization update requested for organization ID '{}'.", id);
        if (!this.securityUtil.isSuperAdmin()){
            throw new UnauthorizedException("Only super admin can update organization");
        }
        this.permissionService.requirePermission(PermissionConstants.UPDATE_ORGANIZATION);

        final Organization organization = this.organizationRepository.findByIdAndActiveTrue(id).orElseThrow(() ->{
            log.warn("Organization update failed because organization ID '{}' was not found.", id);
            return new ResourceNotFoundException("Organization not found");});

        if (request.getName() == null || request.getName().isBlank()) {
            log.warn("Organization update failed because no organization name was provided.");
            throw new BadRequestException("Organization name is required");
        }

        final String organizationName = request.getName().trim();
        if (organization.getName().equalsIgnoreCase(organizationName)) {
            log.warn("Organization update failed because organization '{}' already has the name '{}'.", organization.getId(), organizationName);
            throw new ConflictException("Cannot update to same name");
        }
        if(this.organizationRepository.existsByName(organizationName)) {
            log.warn("Organization update failed because organization name '{}' already exists.", organizationName);
            throw new ConflictException("Organization name already exists");
        }

        organization.setName(organizationName);
        final Organization savedOrganization = this.organizationRepository.save(organization);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action(AuditActionConstants.UPDATE_ORGANIZATION)
                        .entityAffected(EntityAffectedConstants.ORGANIZATION)
                        .entityId(savedOrganization.getId())
                        .description("Updated organization: "
                                + savedOrganization.getName())
                        .build());
        log.info("Organization '{}' updated successfully.", savedOrganization.getId());

        return OrganizationResponseDto.builder()
                .id(savedOrganization.getId())
                .name(savedOrganization.getName())
                .active(savedOrganization.getActive())
                .createdAt(savedOrganization.getCreatedAt())
                .build();
    }

    private User createInactiveOrganizationAdmin(final NewOrganizationAdminData data){

        final String temporaryPassword = this.passwordGeneratorService.generatePassword(ApplicationConstants.GENERATED_PASSWORD_LENGTH);
        final String encodedPassword = this.passwordEncoder.encode(temporaryPassword);

        final User orgAdmin = User.builder()
                .email(data.getEmail())
                .password(encodedPassword)
                .firstName(data.getFirstName())
                .lastName(data.getLastName())
                .role(data.getRole())
                .organization(data.getOrganization())
                .active(false)
                .mfaEnabled(false)
                .build();

        return this.prepareOrgAdminForActivation(orgAdmin);
    }

    private User prepareOrgAdminForActivation(final User user){

        final String activationToken = this.tokenGeneratorService.generateToken();
        final Instant activationTokenExpiresAt = Instant.now().plus(ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS, ChronoUnit.HOURS);

        user.setActivationToken(activationToken);
        user.setActivationTokenExpiresAt(activationTokenExpiresAt);

        final User savedUser = this.userRepository.save(user);
        final String activationLink = this.baseUrl + "/auth/activate-account?token=" + activationToken;

        this.emailService.sendActivationMail(savedUser.getEmail(), activationLink);
        return savedUser;
    }

    @Getter
    @Builder
    private static class NewOrganizationAdminData {

        private final String email;
        private final String firstName;
        private final String lastName;
        private final Role role;
        private final Organization organization;
    }
}

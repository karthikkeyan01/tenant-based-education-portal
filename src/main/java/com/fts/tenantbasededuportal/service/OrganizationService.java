package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.audit.AuditRequestDto;
import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationResponseDto;
import com.fts.tenantbasededuportal.dto.organization.OrganizationRequestDto;
import com.fts.tenantbasededuportal.dto.organization.OrganizationResponseDto;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.Role;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.RoleRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.constants.*;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import jakarta.transaction.Transactional;
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

        if (!this.securityUtil.isSuperAdmin()){

            throw new UnauthorizedException("You are not allowed to perform this action");
        }

        this.permissionService.requirePermission(PermissionConstants.CREATE_ORGANIZATION);

        if (this.organizationRepository.existsByName(
                request.getOrganizationName())) {

            throw new BadRequestException("Organization already exists.");
        }

        if (this.userRepository.existsByEmail(
                request.getOrgAdminEmail())){

            throw new BadRequestException("User already exists.");
        }

        final Role orgAdminRole =
                this.roleRepository.findByName(RoleConstants.ORG_ADMIN)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Role Not Found."));

        final String temporaryPassword = this.passwordGeneratorService.generatePassword(
                        ApplicationConstants.GENERATED_PASSWORD_LENGTH);

        final String encodedPassword = passwordEncoder.encode(temporaryPassword);

        final String activationToken = this.tokenGeneratorService.generateToken();

        final Instant activationTokenExpiresAt =
                Instant.now().plus(
                        ApplicationConstants.ACTIVATION_LINK_EXPIRY_HOURS,
                        ChronoUnit.HOURS);

        final Organization organization = Organization.builder()
                .name(request.getOrganizationName())
                .active(true)
                .build();

        final Organization savedOrganization =
                this.organizationRepository.save(organization);

        final User orgAdmin = User.builder()
                .email(request.getOrgAdminEmail())
                .password(encodedPassword)
                .firstName(request.getOrgAdminFirstName())
                .lastName(request.getOrgAdminLastName())
                .role(orgAdminRole)
                .organization(savedOrganization)
                .active(false)
                .mfaEnabled(false)
                .activationToken(activationToken)
                .activationTokenExpiresAt(activationTokenExpiresAt)
                .build();

        final User savedUser = this.userRepository.save(orgAdmin);

        final String activationLink = this.baseUrl
                + "/auth/activate-account?token="
                + activationToken;

        this.emailService.sendActivationMail(
                savedUser.getEmail(), activationLink);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action(AuditActionConstants.CREATE_ORGANIZATION)
                        .entityAffected(EntityAffectedConstants.ORGANIZATION)
                        .entityId(savedOrganization.getId())
                        .description("Created organization: "
                                + savedOrganization.getName()
                                + " along with it's admin")
                        .build()
        );

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
    public Page<OrganizationResponseDto> retrieveAllOrganizations(
            final int page, final int size) {

        if (!this.securityUtil.isSuperAdmin()){

            throw new UnauthorizedException(
                    "Only super admin can view organizations");
        }

        this.permissionService.requirePermission(PermissionConstants.VIEW_ORGANIZATIONS);

        final Pageable pageable = PageRequest.of(
                page, size, Sort.by("createdAt").descending());

        final Page<Organization> organizations =
                this.organizationRepository.findByActiveTrue(pageable);

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
    public OrganizationResponseDto retrieveOrganizationById(final String id) {

        if (!this.securityUtil.isSuperAdmin()){

            throw new UnauthorizedException(
                    "Only super admin can view organization");
        }

        this.permissionService.requirePermission(PermissionConstants.VIEW_ORGANIZATIONS);

        final Organization organization = this.organizationRepository
                .findByIdAndActiveTrue(id).orElseThrow(
                () -> new ResourceNotFoundException
                        ("Organization not found"));

        return OrganizationResponseDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .active(organization.getActive())
                .createdAt(organization.getCreatedAt())
                .build();
    }

    //performs a PUT operation and updates organization.
    //can be only performed by super admin.
    public OrganizationResponseDto updateOrganizationById(
            final String id, final OrganizationRequestDto request) {

        if (!this.securityUtil.isSuperAdmin()){

            throw new UnauthorizedException("Only super admin can update organization");
        }

        this.permissionService.requirePermission(PermissionConstants.UPDATE_ORGANIZATION);

        final Organization organization =
                this.organizationRepository.findByIdAndActiveTrue(id).orElseThrow(
                        () -> new ResourceNotFoundException
                                ("Organization not found"));

        if (request.getName() == null || request.getName().isBlank()) {

            throw new BadRequestException
                    ("Organization name is required");
        }

        final String organizationName = request.getName().trim();

        if (organization.getName().equalsIgnoreCase(organizationName)) {

            throw new BadRequestException(
                    "Cannot update to same name");
        }

        if(this.organizationRepository
                .existsByName(organizationName)) {

            throw new BadRequestException
                    ("Organization name already exists");
        }

        organization.setName(organizationName);

        final Organization savedOrganization =
                this.organizationRepository.save(organization);

        this.auditService.create(
                AuditRequestDto.builder()
                        .action(AuditActionConstants.UPDATE_ORGANIZATION)
                        .entityAffected(EntityAffectedConstants.ORGANIZATION)
                        .entityId(savedOrganization.getId())
                        .description("Updated organization: "
                                + savedOrganization.getName())
                        .build());

        return OrganizationResponseDto.builder()
                .id(savedOrganization.getId())
                .name(savedOrganization.getName())
                .active(savedOrganization.getActive())
                .createdAt(savedOrganization.getCreatedAt())
                .build();
    }
}

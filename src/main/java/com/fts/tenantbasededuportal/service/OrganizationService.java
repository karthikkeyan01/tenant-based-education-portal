package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.ResourceNotFoundException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import com.fts.tenantbasededuportal.repository.UserRepository;
import com.fts.tenantbasededuportal.util.RoleConstants;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final SecurityUtil securityUtil;

    private final UserRepository userRepository;

    private final AuditService auditService;

    //performs a POST operation and creates an organization.
    //only super admin can create organizations
    public Organization createOrganization(final CreateOrganizationRequestDto request) {

        final User currentUser = securityUtil.getCurrentUser();

        final Optional<Organization> existingOrganization = this.organizationRepository.
                findByName(request.getName());

        if (existingOrganization.isPresent()) {

            if (Boolean.TRUE.equals(existingOrganization.get().getDeleted())){

                throw new BadRequestException
                        ("Organization exists but is inactive. Restore it instead");
            }

            throw new BadRequestException("Organization already exists");
        }

        final Organization organization = Organization.builder()
                .name(request.getName())
                .build();

        this.organizationRepository.save(organization);

        this.auditService.log(
                currentUser,
                "CREATE_ORGANIZATION",
                "ORGANIZATION",
                organization.getId(),
                "Created organization " + organization.getName());

        return organization;

    }

    //performs a GET operation and fetches all organizations.
    //can be performed only by super admin.
    public List<Organization> fetchAllOrganizations() {

        final User currentUser = securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        //checks if logged-in user is super admin.
        if (RoleConstants.SUPER_ADMIN.equals(roleName)) {

            this.auditService.log(
                    currentUser,
                    "VIEW_ORGANIZATIONS",
                    "ORGANIZATION",
                    null,
                    "Viewed organizations list");

            return this.organizationRepository.findByDeletedFalse();

        }
        else {

            throw new UnauthorizedException(
                    "You are not authorized to perform this action");
        }
    }

    //performs a GET operation and fetches organization based on org id.
    //can be accessed by super admin.
    public Organization fetchOrganizationById(final String id) {

        final User currentUser = securityUtil.getCurrentUser();

        final Organization organization = this.organizationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException
                        ("Organization with id " + id + " not found"));

        if(organization.getDeleted()) {

            throw new ResourceNotFoundException("Organization is not found");
        }

        this.auditService.log(
                currentUser,
                "VIEW_ORGANIZATION",
                "ORGANIZATION",
                organization.getId(),
                "Viewed organization " + organization.getName());

        return organization;
    }

    //performs a PUT operation and updates organization.
    //can be only performed by super admin.
    public Organization updateOrganizationById(
            final String id, final Organization request) {

        final User currentUser = securityUtil.getCurrentUser();

        final Organization organization =
                this.organizationRepository.findByIdAndDeletedFalse(id).orElseThrow(
                        () -> new ResourceNotFoundException
                                ("Organization with id " + id + " not found"));

        if (request.getName() == null){

            throw new BadRequestException
                    ("Organization name is required");
        }

        if(!organization.getName().equals(request.getName())
                && this.organizationRepository
                .existsByName(request.getName())) {

            throw new BadRequestException
                    ("Organization name already exists");
        }

        organization.setName(request.getName());

        this.organizationRepository.save(organization);

        this.auditService.log(
                currentUser,
                "UPDATE_ORGANIZATION",
                "ORGANIZATION",
                organization.getId(),
                "Updated organization " + organization.getName()
        );

        return organization;
    }

    //performs a DELETE operation and deletes the organization.
    //can only be performed by super admin.
    public void deleteOrganization(final String id){

        final User currentUser = securityUtil.getCurrentUser();

        final Organization organization = this.organizationRepository
                .findById(id).orElseThrow(()->
                        new ResourceNotFoundException(
                                "Organization with id " + id + " not found"));

        final List<User> users = this.userRepository.findByOrganization(organization);

        if(organization.getDeleted()){

            throw new BadRequestException("Organization already deleted");
        }

        for (User user : users){

            user.setDeleted(true);
        }

        this.userRepository.saveAll(users);

        organization.setDeleted(true);

        this.organizationRepository.save(organization);

        this.auditService.log(
                currentUser,
                "DELETE_ORGANIZATION",
                "ORGANIZATION",
                organization.getId(),
                "Deleted organization " + organization.getName());
    }

    public void restoreOrganization(final String id){

        final User currentUser = securityUtil.getCurrentUser();

        final Organization organization = organizationRepository.findById(id).orElseThrow(
                ()-> new ResourceNotFoundException("Organization not found"));

        if(!organization.getDeleted()){

            throw new BadRequestException("Organization already active");
        }

        organization.setDeleted(false);

//        List<User> users = userRepository.findByOrganization(organization);
//
//        for (User user : users){
//
//            user.setDeleted(false);
//        }
//
//        userRepository.saveAll(users);

        organizationRepository.save(organization);

        auditService.log(
                currentUser,
                "RESTORE_ORGANIZATION",
                "ORGANIZATION",
                organization.getId(),
                "Restored organization " + organization.getName());
    }
}

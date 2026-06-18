package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.organization.CreateOrganizationRequestDto;
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

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    private final SecurityUtil securityUtil;

    private final UserRepository userRepository;

    public Organization createOrganization(final CreateOrganizationRequestDto request) {

        if (this.organizationRepository.existsByName(request.getName())) {

            throw new BadRequestException("Organization already exists");
        }

        final Organization organization = Organization.builder()
                .name(request.getName())
                .build();

        return this.organizationRepository.save(organization);

    }

    public List<Organization> fetchAllOrganizations() {

        final User currentUser = securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        if (RoleConstants.SUPER_ADMIN.equals(roleName)) {

            return this.organizationRepository.findAll();
        } else if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            return List.of(currentUser.getOrganization());
        } else {

            throw new UnauthorizedException(
                    "You are not authorized to perform this action");
        }
    }

    public Organization fetchOrganizationById(final String id) {

        final User currentUser = securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        final Organization organization = this.organizationRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException
                        ("Organization with id " + id + " not found"));

        if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            if(currentUser.getOrganization() == null
            || !currentUser.getOrganization().getId().equals(id)){

                throw new UnauthorizedException(
                        "you cannot access another organization");
            }
        }

        return organization;
    }

    public Organization updateOrganizationById(
            final String id, final Organization request) {

        final Organization organization =
                this.organizationRepository.findById(id).orElseThrow(
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

        return this.organizationRepository.save(organization);
    }

    public void deleteOrganization(final String id){

        final Organization organization = this.organizationRepository
                .findById(id).orElseThrow(()->
                        new ResourceNotFoundException(
                                "Organization with id " + id + " not found"));

        if (this.userRepository.existsByOrganization(organization)) {

            throw new BadRequestException
                    ("Organization contains users and cannot be deleted");
        }

        this.organizationRepository.delete(organization);
    }
}

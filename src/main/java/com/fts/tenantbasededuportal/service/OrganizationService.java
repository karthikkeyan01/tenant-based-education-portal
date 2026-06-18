package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.entity.User;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.exception.UnauthorizedException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
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

    public Organization createOrganization (final CreateOrganizationRequestDto request){

        if (this.organizationRepository.existsByName(request.getName())){

            throw new BadRequestException("Organization already exists");
        }

        final Organization organization = Organization.builder()
                .name(request.getName())
                .build();

        return this.organizationRepository.save(organization);

    }

    public List<Organization> fetchAllOrganizations(){

        final User currentUser = securityUtil.getCurrentUser();

        final String roleName = currentUser.getRole().getName();

        if (RoleConstants.SUPER_ADMIN.equals(roleName)){

            return this.organizationRepository.findAll();
        }
        else if (RoleConstants.ORG_ADMIN.equals(roleName)) {

            return List.of(currentUser.getOrganization());
        }
        else {

            throw new UnauthorizedException(
                    "You are not authorized to perform this action");
        }

    }
}

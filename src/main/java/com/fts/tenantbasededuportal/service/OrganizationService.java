package com.fts.tenantbasededuportal.service;

import com.fts.tenantbasededuportal.dtos.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.dtos.organization.OrganizationResponseDto;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.exception.BadRequestException;
import com.fts.tenantbasededuportal.repository.OrganizationRepository;
import jakarta.persistence.EntityExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationResponseDto createOrganization (final CreateOrganizationRequestDto request){

        if (this.organizationRepository.existsByName(request.getName())){

            throw new BadRequestException("Organization already exists");
        }

        final Organization organization = Organization.builder()
                .name(request.getName())
                .build();

        this.organizationRepository.save(organization);

        return OrganizationResponseDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .createdAt(organization.getCreatedAt())
                .build();
    }
}

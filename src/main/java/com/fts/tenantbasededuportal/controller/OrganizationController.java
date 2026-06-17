package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dtos.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.dtos.organization.OrganizationResponseDto;
import com.fts.tenantbasededuportal.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PreAuthorize("hasAuthority('CREATE_ORGANIZATION')")
    @PostMapping
    public OrganizationResponseDto createOrganization(
            @RequestBody final CreateOrganizationRequestDto request
    ) {

        return this.organizationService.createOrganization(request);
    }
}
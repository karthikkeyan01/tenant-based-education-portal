package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dtos.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PreAuthorize("hasAuthority('CREATE_ORGANIZATION')")
    @PostMapping
    public Organization createOrganization(
            @RequestBody final CreateOrganizationRequestDto request
    ) {

        return this.organizationService.createOrganization(request);
    }

    @PreAuthorize("hasAuthority('VIEW_ORGANIZATIONS')")
    @GetMapping
    public List<Organization> fetchAllOrganizations(){

        return this.organizationService.fetchAllOrganizations();
    }
}
package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.entity.Organization;
import com.fts.tenantbasededuportal.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    @PreAuthorize("hasAuthority('VIEW_ORGANIZATIONS')")
    @GetMapping("/{id}")
    public Organization fetchOrganizationById(@PathVariable final String id) {

        return this.organizationService.fetchOrganizationById(id);
    }

    @PreAuthorize(("hasAuthority('UPDATE_ORGANIZATION')"))
    @PutMapping("/{id}")
    public Organization updateOrganization(
            @PathVariable final String id,
            @RequestBody final Organization request) {

        return this.organizationService.updateOrganizationById(id,request);
    }

    @PreAuthorize("hasAuthority('DELETE_ORGANIZATION')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganizationById
            (@PathVariable final String id) {

        this.organizationService.deleteOrganization(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('RESTORE_ORGANIZATION')")
    @PutMapping("/{id}/restore")
    public ResponseEntity<Void> restoreOrganization(@PathVariable final String id){

        this.organizationService.restoreOrganization(id);

        return ResponseEntity.noContent().build();
    }
}
package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationResponseDto;
import com.fts.tenantbasededuportal.dto.organization.OrganizationRequestDto;
import com.fts.tenantbasededuportal.dto.organization.OrganizationResponseDto;
import com.fts.tenantbasededuportal.service.OrganizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto<CreateOrganizationResponseDto>> createOrganization(
            @RequestBody final CreateOrganizationRequestDto request) {

        final CreateOrganizationResponseDto response  =
                this.organizationService.createOrganization(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.<CreateOrganizationResponseDto>builder()
                                .code(HttpStatus.CREATED.value())
                                .message("Created organization with its admin.")
                                .data(response)
                                .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponseDto<Page<OrganizationResponseDto>>>
    retrieveAllOrganizations(@RequestParam(defaultValue = "0")final int page,
                          @RequestParam(defaultValue = "10")final int size){

        final Page<OrganizationResponseDto> response =
                this.organizationService.retrieveAllOrganizations(page, size);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<OrganizationResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Organizations retrieved successfully")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<OrganizationResponseDto>>
    retrieveOrganizationById(@PathVariable final String id) {

        final OrganizationResponseDto response =
                this.organizationService.retrieveOrganizationById(id);

        return ResponseEntity.ok(
                ApiResponseDto.<OrganizationResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("Organization retrieved successfully")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<OrganizationResponseDto>> updateOrganization(
            @PathVariable final String id,
            @RequestBody final OrganizationRequestDto request) {

        final OrganizationResponseDto response =
                this.organizationService.updateOrganizationById(id, request);

        return ResponseEntity.ok(
                ApiResponseDto.<OrganizationResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("Updated organization successfully")
                        .data(response)
                        .build());
    }
}
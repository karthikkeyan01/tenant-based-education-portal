package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationRequestDto;
import com.fts.tenantbasededuportal.dto.organization.CreateOrganizationResponseDto;
import com.fts.tenantbasededuportal.dto.organization.OrganizationRequestDto;
import com.fts.tenantbasededuportal.dto.organization.OrganizationResponseDto;
import com.fts.tenantbasededuportal.service.OrganizationService;
import com.fts.tenantbasededuportal.util.constants.SecurityConstants;
import com.fts.tenantbasededuportal.util.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Organization Management", description = "APIs for managing organizations.")
@SecurityRequirement(name = SwaggerConstants.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/organizations")
@RequiredArgsConstructor
@Validated
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "Create organization",
            description = "Creates a new organization along with its administrator account and sends an account activation email.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Organization created successfully along with its administrator."),
            @ApiResponse(responseCode = "409", description = "Organization or administrator email already exists."),
            @ApiResponse(responseCode = "500", description = "Failed to send the account activation email.")})
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<CreateOrganizationResponseDto> createOrganization(
            @Valid @RequestBody final CreateOrganizationRequestDto request) {

        final CreateOrganizationResponseDto response  =
                this.organizationService.createOrganization(request);

        return ApiResponseDto.<CreateOrganizationResponseDto>builder()
                .code(HttpStatus.CREATED.value())
                .message("Organization created successfully along with its administrator.")
                .data(response)
                .build();
    }

    @ApiResponse(responseCode = "200", description = "Organizations retrieved successfully.")
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @GetMapping
    public ApiResponseDto<Page<OrganizationResponseDto>> retrieveAllOrganizations(
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size){

        final Page<OrganizationResponseDto> response =
                this.organizationService.retrieveAllOrganizations(page, size);

        return ApiResponseDto.<Page<OrganizationResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message("Organizations retrieved successfully.")
                .data(response)
                .build();
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "Organization retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Organization not found.")})
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @GetMapping("/{id}")
    public ApiResponseDto<OrganizationResponseDto> retrieveOrganizationById(@PathVariable final String id) {

        final OrganizationResponseDto response =
                this.organizationService.retrieveOrganizationById(id);

        return ApiResponseDto.<OrganizationResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("Organization retrieved successfully.")
                .data(response)
                .build();
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "Organization updated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid or duplicate organization name."),
            @ApiResponse(responseCode = "404", description = "Organization not found.")})
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @PutMapping("/{id}")
    public ApiResponseDto<OrganizationResponseDto> updateOrganization(
            @PathVariable final String id,
            @Valid @RequestBody final OrganizationRequestDto request) {

        final OrganizationResponseDto response =
                this.organizationService.updateOrganizationById(id, request);

        return ApiResponseDto.<OrganizationResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("Organization updated successfully.")
                .data(response)
                .build();
    }
}
package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.user.*;
import com.fts.tenantbasededuportal.service.UserService;
import com.fts.tenantbasededuportal.util.constants.SecurityConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_SUPER_OR_ORG_ADMIN)
    public ApiResponseDto<Page<UserResponseDto>> retrieveUsers(
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size) {

        final Page<UserResponseDto> response = this.userService.retrieveUsers(page, size);

        return ApiResponseDto.<Page<UserResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message("Users fetched successfully.")
                .data(response)
                .build();
    }

    @PreAuthorize(SecurityConstants.HAS_SUPER_OR_ORG_ADMIN)
    @GetMapping("/{id}")
    public ApiResponseDto<UserResponseDto> retrieveUserById(@PathVariable final String id) {

        final UserResponseDto response = this.userService.retrieveUserById(id);

        return ApiResponseDto.<UserResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("User retrieved successfully.")
                .data(response)
                .build();
    }

    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @GetMapping("/organizations/{organizationId}")
    public ApiResponseDto<Page<UserResponseDto>> retrieveUsersByOrganization(
            @PathVariable final String organizationId,
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10")  final int size) {

        final Page<UserResponseDto> response = this.userService.
                retrieveUsersByOrganization(organizationId, page, size);

        return ApiResponseDto.<Page<UserResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message("Users retrieved successfully.")
                .data(response)
                .build();
    }

    @PreAuthorize(SecurityConstants.HAS_ORG_ADMIN)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<UserResponseDto> createUser(@Valid @RequestBody final CreateUserRequestDto request) {

        final UserResponseDto response = this.userService.createUser(request);

        return ApiResponseDto.<UserResponseDto>builder()
                .code(HttpStatus.CREATED.value())
                .message("User created successfully.")
                .data(response)
                .build();
    }

    @PreAuthorize(SecurityConstants.HAS_ORG_ADMIN)
    @PutMapping("/{userId}/activate")
    public ApiResponseDto<Void> activate(
            @PathVariable final String userId,
            @RequestParam final boolean active) {

        this.userService.activate(userId, active);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(active
                        ? "User activated successfully."
                        : "User deactivated successfully.")
                .build();
    }

    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @PutMapping("/activate")
    public ApiResponseDto<Void> activate(
            @RequestParam final String id,
            @RequestParam final boolean active,
            @RequestParam final boolean isUser) {

        this.userService.activate(id, active, isUser);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(active
                        ? "Activation completed successfully."
                        : "Deactivation completed successfully.")
                .build();
    }

    @PreAuthorize(SecurityConstants.HAS_SUPER_OR_ORG_ADMIN)
    @PostMapping("/bulk-upload")
    public ApiResponseDto<BulkUploadResponseDto> uploadBulk(
            @RequestParam("file") final MultipartFile file,
            @RequestParam(required = false) final String organizationId){

        final BulkUploadResponseDto response = this.userService
                .bulkUploadUsers(file, organizationId);

        return ApiResponseDto.<BulkUploadResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("Bulk upload completed successfully.")
                .data(response)
                .build();
    }

    @PreAuthorize(SecurityConstants.HAS_SUPER_OR_ORG_ADMIN)
    @PostMapping("/{userId}/resend-activation")
    public ApiResponseDto<Void> resendActivationEmail(
            @PathVariable final String userId) {

        this.userService.resendActivationEmail(userId);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Activation email sent successfully.")
                .build();
    }
}

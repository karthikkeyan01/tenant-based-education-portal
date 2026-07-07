package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.user.*;
import com.fts.tenantbasededuportal.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN')")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> retrieveUsers(
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size) {

        final Page<UserResponseDto> response =
                this.userService.retrieveUsers(page, size);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<UserResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Users fetched successfully.")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserResponseDto>> retrieveUserById(
            @PathVariable final String id) {

        final UserResponseDto response = this.userService.retrieveUserById(id);

        return ResponseEntity.ok(
                ApiResponseDto.<UserResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("User retrieved successfully.")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/organizations/{organizationId}")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>>
    retrieveUsersByOrganization(
            @PathVariable final String organizationId,
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10")  final int size) {

        final Page<UserResponseDto> response =
                this.userService.retrieveUsersByOrganization(organizationId,
                        page, size);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<UserResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Users retrieved successfully.")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponseDto<UserResponseDto>> createUser(
            @Valid
            @RequestBody final CreateUserRequestDto request) {

        final UserResponseDto response =
                this.userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.<UserResponseDto>builder()
                                .code(HttpStatus.CREATED.value())
                                .message("User created successfully.")
                                .data(response)
                                .build());
    }

    @PreAuthorize("hasRole('ORG_ADMIN')")
    @PutMapping("/{userId}/activate")
    public ResponseEntity<ApiResponseDto<Void>> activate(
            @PathVariable final String userId,
            @RequestParam final boolean active) {

        this.userService.activate(userId, active);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message(active
                                ? "User activated successfully."
                                : "User deactivated successfully.")
                        .build());
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PutMapping("/activate")
    public ResponseEntity<ApiResponseDto<Void>> activate(
            @RequestParam final String id,
            @RequestParam final boolean active,
            @RequestParam final boolean isUser) {

        this.userService.activate(id, active, isUser);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message(active
                                ? "Activation completed successfully."
                                : "Deactivation completed successfully.")
                        .build());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    @PostMapping("/bulk-upload")
    public ResponseEntity<ApiResponseDto<BulkUploadResponseDto>> uploadBulk(
            @RequestParam("file") final MultipartFile file,
            @RequestParam(required = false) final String organizationId){

        final BulkUploadResponseDto response = this.userService
                .bulkUploadUsers(file, organizationId);

        return ResponseEntity.ok(
                ApiResponseDto.<BulkUploadResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("Bulk upload completed successfully.")
                        .data(response)
                        .build());
    }

    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ORG_ADMIN')")
    @PostMapping("/{userId}/resend-activation")
    public ResponseEntity<ApiResponseDto<Void>> resendActivationEmail(
            @PathVariable final String userId) {

        this.userService.resendActivationEmail(userId);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Activation email sent successfully.")
                        .build());
    }
}

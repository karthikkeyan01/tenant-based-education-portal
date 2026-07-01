package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.user.*;
import com.fts.tenantbasededuportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORG_ADMIN')")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>> fetchUsers(
            final Pageable pageable) {

        final Page<UserResponseDto> users =
                this.userService.retrieveUsers(pageable);

        return ResponseEntity.ok(
                ApiResponseDto.<Page<UserResponseDto>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Users fetched successfully.")
                        .data(users)
                        .build());
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping("/{id}")
    public UserResponseDto fetchUserById(@PathVariable final String id) {
        return this.userService.retrieveUserById(id);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/organizations/{organizationId}")
    public ResponseEntity<ApiResponseDto<Page<UserResponseDto>>>
    retrieveUsersByOrganization(
            @PathVariable final String organizationId,
            @RequestParam(defaultValue = "0") final int page,
            @RequestParam(defaultValue = "10")  final int size) {

        final Page<UserResponseDto> response =
                this.userService.retrieveUsersByOrganization(organizationId,
                        page, size);

        return ResponseEntity.ok(
                new ApiResponseDto<>(
                        HttpStatus.OK.value(),
                        "Users retrieved successfully.",
                        response));
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public UserResponseDto createUser(
            @RequestBody final CreateUserRequestDto request) {

        return this.userService.createUser(request);
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/bulk-upload")
    public BulkUploadResponseDto bulkUploadFile
            (@RequestParam("file") final MultipartFile file,
             @RequestParam(required = false) final String organizationId) {

        return this.userService.bulkUploadUsers(file, organizationId);
    }
}

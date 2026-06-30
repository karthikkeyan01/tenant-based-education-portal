package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.user.*;
import com.fts.tenantbasededuportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
                this.userService.fetchUsers(pageable);

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
        return this.userService.fetchUserById(id);
    }

    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping
    public UserResponseDto createUser(
            @RequestBody final CreateUserRequestDto request) {

        return this.userService.createUser(request);
    }

    @PreAuthorize("hasAuthority('UPDATE_USER')")
    @PutMapping("/{id}")
    public UserResponseDto updateUser(
            @PathVariable final String id,
            @RequestBody final UpdateUserRequestDto request
    ) {

        return this.userService.updateUser(id, request);
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable final String id) {

        this.userService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
    @PostMapping("/bulk-upload")
    public BulkUploadResponseDto bulkUploadFile
            (@RequestParam("file") final MultipartFile file,
             @RequestParam(required = false) final String organizationId) {

        return this.userService.bulkUploadUsers(file, organizationId);
    }

    @PreAuthorize("hasAuthority('MANAGE_USER')")
    @PutMapping("/{id}/restore")
    public UserResponseDto restoreUser(@PathVariable final String id,
                                       @RequestBody final RestoreUserRequestDto request){

        return this.userService.restoreUser(id, request);
    }

    @PreAuthorize("hasAuthority('DELETE_USER')")
    @DeleteMapping("/organization/{organizationId}")
    public ResponseEntity<Void> deleteUsersByOrganization(
            @PathVariable final String organizationId) {

        this.userService.deleteUsersByOrganization(organizationId);

        return ResponseEntity.noContent().build();
    }
}

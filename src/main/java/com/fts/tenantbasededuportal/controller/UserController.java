package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dtos.user.BulkUploadResponseDto;
import com.fts.tenantbasededuportal.dtos.user.CreateUserRequestDto;
import com.fts.tenantbasededuportal.dtos.user.UpdateUserRequestDto;
import com.fts.tenantbasededuportal.dtos.user.UserResponseDto;
import com.fts.tenantbasededuportal.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping
    public List<UserResponseDto> users(){

        return userService.fetchUsers();
    }

    @PreAuthorize("hasAuthority('VIEW_USERS')")
    @GetMapping("/{id}")
    public UserResponseDto fetchUserById(@PathVariable final String id) {
        return this.userService.fetchUserById(id);
    }

    @PreAuthorize("hasAuthority('CREATE_USER')")
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
    public BulkUploadResponseDto uploadFile
            (@RequestParam("file") final MultipartFile file) {

        return this.userService.bulkUploadUsers(file);
    }
}

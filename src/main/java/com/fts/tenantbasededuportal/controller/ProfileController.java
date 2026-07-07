package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.profile.ChangePasswordRequestDto;
import com.fts.tenantbasededuportal.dto.profile.ProfileResponseDto;
import com.fts.tenantbasededuportal.dto.profile.UpdateProfileRequestDto;
import com.fts.tenantbasededuportal.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Validated
public class ProfileController {

    private final ProfileService profileService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public ResponseEntity<ApiResponseDto<ProfileResponseDto>> retrieveProfile(){

        final ProfileResponseDto response = this.profileService.retrieveProfile();

        return ResponseEntity.ok(
                ApiResponseDto.<ProfileResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("Profile retrieved successfully.")
                        .data(response)
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping
    public ResponseEntity<ApiResponseDto<ProfileResponseDto>>
    updateProfile(@Valid @RequestBody final UpdateProfileRequestDto request) {

        final ProfileResponseDto response =
                this.profileService.updateProfile(request);

        return ResponseEntity.ok(
                ApiResponseDto.<ProfileResponseDto>builder()
                        .code(HttpStatus.OK.value())
                        .message("Profile updated successfully.")
                        .data(response)
                        .build());
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/change-password")
    public ResponseEntity<ApiResponseDto<Void>>
    changePassword(@Valid @RequestBody final ChangePasswordRequestDto request) {

        this.profileService.changeProfilePassword(request);

        return ResponseEntity.ok(
                ApiResponseDto.<Void>builder()
                        .code(HttpStatus.OK.value())
                        .message("Password changed successfully.")
                        .build());
    }
}

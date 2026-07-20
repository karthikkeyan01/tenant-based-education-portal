package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.profile.ChangePasswordRequestDto;
import com.fts.tenantbasededuportal.dto.profile.ProfileResponseDto;
import com.fts.tenantbasededuportal.dto.profile.UpdateProfileRequestDto;
import com.fts.tenantbasededuportal.service.ProfileService;
import com.fts.tenantbasededuportal.util.constants.SecurityConstants;
import com.fts.tenantbasededuportal.util.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Profile Management", description = "APIs for managing the authenticated user's profile.")
@SecurityRequirement(name = SwaggerConstants.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Validated
public class ProfileController {

    private final ProfileService profileService;

    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully.")
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @GetMapping
    public ApiResponseDto<ProfileResponseDto> retrieveProfile() {

        final ProfileResponseDto response = this.profileService.retrieveProfile();

        return ApiResponseDto.<ProfileResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("Profile retrieved successfully.")
                .data(response)
                .build();
    }

    @Operation(summary = "Update profile.", description = "Updates one or more details of the authenticated user except password.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Profile updated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid profile details.")})
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @PutMapping
    public ApiResponseDto<ProfileResponseDto> updateProfile(
            @Valid @RequestBody final UpdateProfileRequestDto request) {

        final ProfileResponseDto response = this.profileService.updateProfile(request);

        return ApiResponseDto.<ProfileResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("Profile updated successfully.")
                .data(response)
                .build();
    }

    @Operation(summary = "Change password",
            description = "Changes the password of the authenticated user after verifying the current password.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Password changed successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid password details.")})
    @PreAuthorize(SecurityConstants.IS_AUTHENTICATED)
    @PutMapping("/change-password")
    public ApiResponseDto<Void> changePassword(
            @Valid @RequestBody final ChangePasswordRequestDto request) {

        this.profileService.changeProfilePassword(request);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message("Password changed successfully.")
                .build();
    }
}

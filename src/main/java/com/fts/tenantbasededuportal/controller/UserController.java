package com.fts.tenantbasededuportal.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.dto.user.*;
import com.fts.tenantbasededuportal.service.UserService;
import com.fts.tenantbasededuportal.util.constants.SecurityConstants;
import com.fts.tenantbasededuportal.util.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "User Management", description = "APIs for managing users.")
@SecurityRequirement(name = SwaggerConstants.SECURITY_SCHEME_NAME)
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @ApiResponse(responseCode = "200", description = "Users retrieved successfully.")
    @GetMapping
    @PreAuthorize(SecurityConstants.HAS_SUPER_OR_ORG_ADMIN)
    public ApiResponseDto<Page<UserResponseDto>> retrieveUsers(
            @Min(0) @RequestParam(defaultValue = "0") final int page,
            @Min(1) @Max(100) @RequestParam(defaultValue = "10") final int size) {

        final Page<UserResponseDto> response = this.userService.retrieveUsers(page, size);

        return ApiResponseDto.<Page<UserResponseDto>>builder()
                .code(HttpStatus.OK.value())
                .message("Users retrieved successfully.")
                .data(response)
                .build();
    }

    @ApiResponses({@ApiResponse(responseCode = "200", description = "User retrieved successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid user request."),
            @ApiResponse(responseCode = "404", description = "User not found.")})
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

    @Operation(summary = "Retrieve users by organization", description = "Retrieves all active users belonging to a specific organization.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Users retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "Organization not found.")})
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @GetMapping("/organizations/{organizationId}")
    public ApiResponseDto<Page<UserResponseDto>> retrieveUsersByOrganization(
            @Parameter(description = "Organization id which is used to retrieve users of a specific organization.")
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

    @Operation(summary = "Create user",
            description = "Creates a new user account (inactive by default) and sends an account activation email.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "409", description = "User email already exists."),
            @ApiResponse(responseCode = "500", description = "Failed to send the account activation email.")})
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

    @Operation(summary = "Activate or deactivate user",
            description = "Activates or deactivates a user account by an organization admin within the authenticated organization.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User activation status updated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid activation request."),
            @ApiResponse(responseCode = "404", description = "User not found.")})
    @PreAuthorize(SecurityConstants.HAS_ORG_ADMIN)
    @PutMapping("/{userId}/activate")
    public ApiResponseDto<Void> activate(
            @PathVariable final String userId,
            @Parameter(description = "Set to true to activate the user or false to deactivate the user.")
            @RequestParam final boolean active) {

        this.userService.activate(userId, active);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(active
                        ? "User activated successfully."
                        : "User deactivated successfully.")
                .build();
    }

    @Operation(summary = "Activate or deactivate a user or organization",
            description = "Activates or deactivates either an individual user or an organization based on the request parameters.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activation status updated successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid activation request."),
            @ApiResponse(responseCode = "404", description = "User or organization not found.")})
    @PreAuthorize(SecurityConstants.HAS_SUPER_ADMIN)
    @PutMapping("/activate")
    public ApiResponseDto<Void> activate(
            @Parameter(description = "ID of the user or organization to be activated or deactivated.")
            @RequestParam final String id,
            @Parameter(description = "Set to true to activate or false to deactivate.")
            @RequestParam final boolean active,
            @Parameter(description = "Set to true to manage a user or false to manage an organization.")
            @RequestParam final boolean isUser) {

        this.userService.activate(id, active, isUser);

        return ApiResponseDto.<Void>builder()
                .code(HttpStatus.OK.value())
                .message(active
                        ? "Activation completed successfully."
                        : "Deactivation completed successfully.")
                .build();
    }

    @Operation(summary = "Bulk upload users", description = """
                    Uploads users from a CSV or Excel file and sends account activation emails to successfully created users.
                            Supported headers:
                            • email
                            • email,firstName,lastName
                            Organization ID is required only when the authenticated user is a Super Admin.
                    """)
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Bulk upload completed successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid bulk upload request."),
            @ApiResponse(responseCode = "404", description = "Organization not found.")})
    @PreAuthorize(SecurityConstants.HAS_SUPER_OR_ORG_ADMIN)
    @PostMapping("/bulk-upload")
    public ApiResponseDto<BulkUploadResponseDto> uploadBulk(
            @Parameter(description = "CSV or Excel (.xlsx) file containing user data.")
            @RequestParam("file") final MultipartFile file,
            @Parameter(description = "Organization ID. Required only when the authenticated user is a Super Admin.")
            @RequestParam(required = false) final String organizationId){

        final BulkUploadResponseDto response = this.userService
                .bulkUploadUsers(file, organizationId);

        return ApiResponseDto.<BulkUploadResponseDto>builder()
                .code(HttpStatus.OK.value())
                .message("Bulk upload completed successfully.")
                .data(response)
                .build();
    }

    @Operation(summary = "Resend activation email",
            description = "Generates a new account activation link with token and sends it to the user's email address.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Activation email sent successfully."),
            @ApiResponse(responseCode = "400", description = "Invalid activation email request."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "500", description = "Failed to send the activation email.")})
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

package com.fts.tenantbasededuportal.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Schema(description = "Request payload for changing the user's password.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDto {

    @Schema(description = "Current password of the authenticated user.", example = "OldPassword@123")
    @NotBlank
    @Size(min = 8)
    private String oldPassword;

    @Schema(description = "New password to be set for the user's account.", example = "NewPassword@123")
    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
package com.fts.tenantbasededuportal.dto.profile;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Schema(description = "Request payload for updating the authenticated user's profile.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDto {

    @Schema(description = "User's first name.", example = "John")
    private String firstName;

    @Schema(description = "User's last name.", example = "Doe")
    private String lastName;

    @Schema(description = "Enables or disables multi-factor authentication for the user.", example = "true")
    private Boolean mfaEnabled;
}
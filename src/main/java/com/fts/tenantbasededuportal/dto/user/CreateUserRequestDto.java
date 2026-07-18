package com.fts.tenantbasededuportal.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Schema(description = "Request payload for creating a new user.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequestDto {

    @Schema(description = "Email address of the user.", example = "john.doe@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(description = "User's first name.", example = "John")
    private String firstName;

    @Schema(description = "User's last name.", example = "Doe")
    private String lastName;
}

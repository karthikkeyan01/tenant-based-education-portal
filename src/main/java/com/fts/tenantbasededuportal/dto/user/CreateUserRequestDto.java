package com.fts.tenantbasededuportal.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequestDto {

    @NotBlank
    @Email
    private String email;

    private String firstName;

    private String lastName;
}

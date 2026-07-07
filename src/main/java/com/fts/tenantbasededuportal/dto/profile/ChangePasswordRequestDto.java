package com.fts.tenantbasededuportal.dto.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDto {

    @NotBlank
    @Size(min = 8)
    private String oldPassword;

    @NotBlank
    @Size(min = 8)
    private String newPassword;
}
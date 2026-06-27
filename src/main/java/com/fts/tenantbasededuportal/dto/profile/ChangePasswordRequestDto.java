package com.fts.tenantbasededuportal.dto.profile;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequestDto {

    private String oldPassword;

    private String newPassword;
}
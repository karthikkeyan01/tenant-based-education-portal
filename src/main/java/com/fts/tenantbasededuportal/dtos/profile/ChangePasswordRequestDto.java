package com.fts.tenantbasededuportal.dtos.profile;

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
package com.fts.tenantbasededuportal.dtos.user;

import com.fts.tenantbasededuportal.entity.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDto {

    private String email;

    private String firstName;

    private String secondName;

    private String organizationId;

    private Boolean mfaEnabled;

    private String roleName;
}
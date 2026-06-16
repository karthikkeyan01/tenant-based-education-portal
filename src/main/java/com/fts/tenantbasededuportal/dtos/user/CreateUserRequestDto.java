package com.fts.tenantbasededuportal.dtos.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateUserRequestDto {

    private String email;

    private String password;

    private String firstName;

    private String secondName;

    private String roleName;

    private String organizationId;
}

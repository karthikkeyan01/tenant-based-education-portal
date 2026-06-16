package com.fts.tenantbasededuportal.dtos.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateUserRequestDto {

    private String firstName;

    private String secondName;

    private Boolean disabled;
}
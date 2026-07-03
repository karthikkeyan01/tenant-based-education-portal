package com.fts.tenantbasededuportal.dto.profile;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDto {

    private String firstName;

    private String lastName;

    private Boolean mfaEnabled;
}
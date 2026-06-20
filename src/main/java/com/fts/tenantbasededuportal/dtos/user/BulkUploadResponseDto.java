package com.fts.tenantbasededuportal.dtos.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadResponseDto {

    private Integer totalRecords;

    private Integer createdRecords;

    private Integer skippedRecords;
}

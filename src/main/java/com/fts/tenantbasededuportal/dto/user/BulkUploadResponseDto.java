package com.fts.tenantbasededuportal.dto.user;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadResponseDto {

    private Integer totalRecords;

    private Integer processedRecords;

    private Integer skippedRecords;
}

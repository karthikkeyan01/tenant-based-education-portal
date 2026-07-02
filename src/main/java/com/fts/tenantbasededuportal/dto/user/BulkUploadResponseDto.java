package com.fts.tenantbasededuportal.dto.user;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadResponseDto {

    private Integer totalRecords;

    private Integer uploadedRecords;

    private Integer skippedRecords;

    private Integer emailFailedRecords;

    private List<String> failedEmails;
}

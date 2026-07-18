package com.fts.tenantbasededuportal.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Schema(description = "Summary of the bulk user upload operation.")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkUploadResponseDto {

    @Schema(description = "Total number of records processed from the uploaded file.", example = "100")
    private Integer totalRecords;

    @Schema(description = "Number of users successfully created.", example = "92")
    private Integer uploadedRecords;

    @Schema(description = "Number of records skipped due to validation errors, duplicate users and other processing issues.", example = "5")
    private Integer skippedRecords;

    @Schema(description = "Number of users created successfully but whose activation emails could not be sent.", example = "3")
    private Integer emailFailedRecords;

    @Schema(description = "Email addresses for which activation emails could not be sent.")
    private List<String> failedEmails;
}

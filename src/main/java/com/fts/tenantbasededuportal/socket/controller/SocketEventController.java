package com.fts.tenantbasededuportal.socket.controller;

import com.fts.tenantbasededuportal.dto.ApiResponseDto;
import com.fts.tenantbasededuportal.socket.dto.SocketEventDto;
import com.fts.tenantbasededuportal.socket.service.SocketEventHistoryService;
import com.fts.tenantbasededuportal.util.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/socket/events")
@RequiredArgsConstructor
@Tag(name = "Socket Events", description = "APIs for retrieving socket event history.")
@SecurityRequirement(name = SwaggerConstants.SECURITY_SCHEME_NAME)
public class SocketEventController {

    private final SocketEventHistoryService  socketEventHistoryService;

    @ApiResponse(responseCode = "200", description = "Socket events retrieved successfully.")
    @GetMapping
    public ApiResponseDto<Page<SocketEventDto>> retrieveEvents(
            @Min(0) @RequestParam(defaultValue = "0") final int page, @Min(1) @Max(100) @RequestParam(defaultValue = "20") final int size) {

        final Page<SocketEventDto> response = this.socketEventHistoryService.retrieveEvents(page, size);

        return ApiResponseDto.<Page<SocketEventDto>>builder()
                .code(HttpStatus.OK.value())
                .message("Socket events retrieved successfully")
                .data(response)
                .build();
    }
}


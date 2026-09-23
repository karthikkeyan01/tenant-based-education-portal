package com.fts.tenantbasededuportal.socket.service;

import com.fts.tenantbasededuportal.socket.dto.SocketEventDto;
import com.fts.tenantbasededuportal.socket.entity.SocketEvent;
import com.fts.tenantbasededuportal.socket.repository.SocketEventRepository;
import com.fts.tenantbasededuportal.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SocketEventHistoryService {

    private final SecurityUtil securityUtil;
    private final SocketEventRepository socketEventRepository;

    @Transactional(readOnly = true)
    public Page<SocketEventDto> retrieveEvents(final int page, final int size) {

        final String userId = this.securityUtil.getCurrentUserId();
        log.info("User '{}' requested event history. (page {}, size {})", userId, page, size);

        final Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        final Page<SocketEvent> socketEvents = this.socketEventRepository.findByRecipientUserId(userId, pageable);

        return socketEvents.map(socketEvent ->
                SocketEventDto.builder()
                        .id(socketEvent.getId())
                        .eventType(socketEvent.getEventType())
                        .payload(socketEvent.getPayload())
                        .createdAt(socketEvent.getCreatedAt())
                        .build());
    }
}

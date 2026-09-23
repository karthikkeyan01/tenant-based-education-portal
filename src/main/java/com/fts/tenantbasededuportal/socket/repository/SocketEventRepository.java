package com.fts.tenantbasededuportal.socket.repository;

import com.fts.tenantbasededuportal.socket.entity.SocketEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SocketEventRepository extends JpaRepository<SocketEvent, String> {

    List<SocketEvent> findByRecipientUserIdAndDeliveredAtIsNullOrderByCreatedAtAsc(String recipientUserId);

    Page<SocketEvent> findByRecipientUserId(String recipientUserId, Pageable pageable);
}

package com.fts.tenantbasededuportal.outbox.repository;

import com.fts.tenantbasededuportal.outbox.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findByPublishedAtIsNullOrderByCreatedAtAsc();
}

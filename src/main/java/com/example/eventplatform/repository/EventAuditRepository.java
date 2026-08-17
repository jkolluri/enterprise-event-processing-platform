package com.example.eventplatform.repository;

import com.example.eventplatform.entity.EventAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface EventAuditRepository extends JpaRepository<EventAudit, UUID> {
    List<EventAudit> findByEventIdOrderByCreatedAtAsc(UUID eventId);
}

package com.example.eventplatform.repository;

import com.example.eventplatform.entity.EventRecord;
import com.example.eventplatform.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface EventRecordRepository extends JpaRepository<EventRecord, UUID> {
    Optional<EventRecord> findByCorrelationId(String correlationId);
    List<EventRecord> findTop50ByStatusOrderByCreatedAtDesc(EventStatus status);
}

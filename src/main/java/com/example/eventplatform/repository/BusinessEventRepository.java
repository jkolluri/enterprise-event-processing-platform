package com.example.eventplatform.repository;

import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BusinessEventRepository extends JpaRepository<BusinessEvent, UUID> {
    List<BusinessEvent> findByStatusOrderByCreatedAtDesc(EventStatus status);
    List<BusinessEvent> findByStatusAndUpdatedAtAfterOrderByUpdatedAtDesc(EventStatus status, Instant after);
    List<BusinessEvent> findByStatusInAndUpdatedAtAfterOrderByUpdatedAtDesc(List<EventStatus> statuses, Instant after);
}

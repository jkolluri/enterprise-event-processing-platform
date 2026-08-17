package com.example.eventplatform.dto;

import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventStatus;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(UUID id, String correlationId, String eventType, String payload,
                            EventStatus status, String errorMessage, int retryCount,
                            Instant createdAt, Instant updatedAt) {
    public static EventResponse from(BusinessEvent e) {
        return new EventResponse(e.getId(), e.getCorrelationId(), e.getEventType(), e.getPayload(),
                e.getStatus(), e.getErrorMessage(), e.getRetryCount(), e.getCreatedAt(), e.getUpdatedAt());
    }
}

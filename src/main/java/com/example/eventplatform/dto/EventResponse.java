package com.example.eventplatform.dto;

import com.example.eventplatform.entity.EventStatus;
import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        UUID id,
        String correlationId,
        String eventType,
        EventStatus status,
        int retryCount,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {}

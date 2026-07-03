package com.example.eventplatform.service;

import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.entity.EventRecord;

public final class EventMapper {
    private EventMapper() {}
    public static EventResponse toResponse(EventRecord event) {
        return new EventResponse(
                event.getId(), event.getCorrelationId(), event.getEventType(), event.getStatus(),
                event.getRetryCount(), event.getFailureReason(), event.getCreatedAt(), event.getUpdatedAt()
        );
    }
}

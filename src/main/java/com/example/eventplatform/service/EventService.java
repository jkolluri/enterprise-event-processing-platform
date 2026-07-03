package com.example.eventplatform.service;

import com.example.eventplatform.dto.EventRequest;
import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.entity.*;
import com.example.eventplatform.exception.ResourceNotFoundException;
import com.example.eventplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRecordRepository eventRepository;
    private final EventAuditRepository auditRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.topic}")
    private String topic;

    @Transactional
    public EventResponse ingest(EventRequest request) {
        eventRepository.findByCorrelationId(request.correlationId()).ifPresent(existing -> {
            throw new IllegalArgumentException("Correlation ID already exists: " + request.correlationId());
        });
        EventRecord event = EventRecord.builder()
                .correlationId(request.correlationId())
                .eventType(request.eventType())
                .payload(request.payload())
                .status(EventStatus.RECEIVED)
                .retryCount(0)
                .build();
        EventRecord saved = eventRepository.save(event);
        audit(saved, EventStatus.RECEIVED, EventStatus.RECEIVED, "Event received");
        kafkaTemplate.send(topic, saved.getCorrelationId());
        return EventMapper.toResponse(saved);
    }

    public EventResponse getById(UUID id) {
        return eventRepository.findById(id).map(EventMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
    }

    public List<EventResponse> latestByStatus(EventStatus status) {
        return eventRepository.findTop50ByStatusOrderByCreatedAtDesc(status).stream().map(EventMapper::toResponse).toList();
    }

    @Transactional
    public EventResponse retry(UUID id) {
        EventRecord event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + id));
        EventStatus old = event.getStatus();
        event.setStatus(EventStatus.RETRYING);
        event.setRetryCount(event.getRetryCount() + 1);
        event.setFailureReason(null);
        eventRepository.save(event);
        audit(event, old, EventStatus.RETRYING, "Manual retry requested");
        kafkaTemplate.send(topic, event.getCorrelationId());
        return EventMapper.toResponse(event);
    }

    @Transactional
    public void updateStatus(EventRecord event, EventStatus newStatus, String message) {
        EventStatus old = event.getStatus();
        event.setStatus(newStatus);
        if (newStatus == EventStatus.FAILED || newStatus == EventStatus.DEAD_LETTER) {
            event.setFailureReason(message);
        }
        eventRepository.save(event);
        audit(event, old, newStatus, message);
    }

    private void audit(EventRecord event, EventStatus oldStatus, EventStatus newStatus, String message) {
        auditRepository.save(EventAudit.builder()
                .eventId(event.getId())
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .message(message)
                .build());
    }
}

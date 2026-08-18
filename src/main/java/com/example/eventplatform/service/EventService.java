package com.example.eventplatform.service;

import com.example.eventplatform.dto.CreateEventRequest;
import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventStatus;
import com.example.eventplatform.messaging.EventProducer;
import com.example.eventplatform.repository.BusinessEventRepository;
import com.example.eventplatform.repository.EventRecordRepository;
import com.example.eventplatform.entity.EventRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EventService {
    private final BusinessEventRepository repository;
    private final EventProducer producer;
    private final AuditService auditService;
    private final EventRecordRepository eventRecordRepository;

    public EventService(BusinessEventRepository repository, EventProducer producer, AuditService auditService, EventRecordRepository eventRecordRepository) {
        this.repository = repository;
        this.producer = producer;
        this.auditService = auditService;
        this.eventRecordRepository = eventRecordRepository;
    }

    @Transactional
    public BusinessEvent create(CreateEventRequest request) {
        BusinessEvent event = new BusinessEvent();
        event.setCorrelationId(request.correlationId());
        event.setEventType(request.eventType());
        event.setPayload(request.payload());
        event.setStatus(EventStatus.RECEIVED);
        event = repository.save(event);
        auditService.record(event, "Event received");
        producer.publish(event.getId());
        return event;
    }

    public List<BusinessEvent> list(EventStatus status) {
        return status == null ? repository.findAll() : repository.findByStatusOrderByCreatedAtDesc(status);
    }

    public BusinessEvent get(UUID id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Event not found: " + id));
    }

    @Transactional
    public BusinessEvent retry(UUID id) {
        BusinessEvent event = get(id);
        if (event.getStatus() != EventStatus.FAILED && event.getStatus() != EventStatus.DEAD_LETTER) {
            throw new IllegalStateException("Only FAILED or DEAD_LETTER events can be retried");
        }
        event.setRetryCount(event.getRetryCount() + 1);
        event.setErrorMessage(null);
        event.setStatus(EventStatus.RETRYING);
        repository.save(event);
        auditService.record(event, "Manual retry requested");
        producer.publish(event.getId());
        return event;
    }

    @Transactional
    public void updateStatus(EventRecord event, EventStatus status, String message) {
        event.setStatus(status);
        event.setFailureReason(message);
        if (status == EventStatus.FAILED || status == EventStatus.DEAD_LETTER) {
            event.setRetryCount(event.getRetryCount() + 1);
        }
        eventRecordRepository.save(event);
    }
}

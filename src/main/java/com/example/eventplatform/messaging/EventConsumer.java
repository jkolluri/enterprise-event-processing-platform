package com.example.eventplatform.messaging;

import com.example.eventplatform.ai.FailureDetectedEvent;
import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventStatus;
import com.example.eventplatform.repository.BusinessEventRepository;
import com.example.eventplatform.service.AuditService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class EventConsumer {
    private final BusinessEventRepository repository;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;
    private final SimpMessagingTemplate webSocket;

    public EventConsumer(BusinessEventRepository repository,
                         AuditService auditService,
                         ApplicationEventPublisher eventPublisher,
                         SimpMessagingTemplate webSocket) {
        this.repository = repository;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
        this.webSocket = webSocket;
    }

    @KafkaListener(topics = "${app.kafka.event-topic:business-events}", groupId = "${spring.kafka.consumer.group-id:event-platform}")
    @Transactional
    public void consume(String eventIdText) {
        UUID eventId = UUID.fromString(eventIdText);
        BusinessEvent event = repository.findById(eventId).orElse(null);
        if (event == null) return;

        event.setStatus(EventStatus.PROCESSING);
        repository.save(event);
        auditService.record(event, "Event processing started");
        publish(event);

        try {
            process(event);
            event.setStatus(EventStatus.SUCCESS);
            event.setErrorMessage(null);
            repository.save(event);
            auditService.record(event, "Event processed successfully");
            publish(event);
        } catch (RuntimeException ex) {
            event.setErrorMessage(ex.getMessage());
            event.setStatus(event.getRetryCount() >= 3 ? EventStatus.DEAD_LETTER : EventStatus.FAILED);
            repository.save(event);
            auditService.record(event, ex.getMessage());
            publish(event);
            eventPublisher.publishEvent(new FailureDetectedEvent(event.getId()));
        }
    }

    private void process(BusinessEvent event) {
        String payload = event.getPayload() == null ? "" : event.getPayload();
        if (payload.contains("\"simulateFailure\":true") || "FAIL_DEMO".equalsIgnoreCase(event.getEventType())) {
            throw new IllegalStateException("Downstream service timeout while processing event");
        }
        if (payload.contains("\"accountId\":null")) {
            throw new IllegalArgumentException("accountId is required and must not be null");
        }
    }

    private void publish(BusinessEvent event) {
        webSocket.convertAndSend("/topic/events", event);
    }
}

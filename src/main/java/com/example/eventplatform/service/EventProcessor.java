package com.example.eventplatform.service;

import com.example.eventplatform.entity.*;
import com.example.eventplatform.repository.EventRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventProcessor {
    private final EventRecordRepository eventRepository;
    private final EventService eventService;
    private final SimpMessagingTemplate messagingTemplate;

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void process(String correlationId) {
        EventRecord event = eventRepository.findByCorrelationId(correlationId).orElseThrow();
        try {
            eventService.updateStatus(event, EventStatus.PROCESSING, "Processing started");
            simulateBusinessRules(event);
            eventService.updateStatus(event, EventStatus.SUCCESS, "Processing completed successfully");
        } catch (Exception ex) {
            EventStatus targetStatus = event.getRetryCount() >= 3 ? EventStatus.DEAD_LETTER : EventStatus.FAILED;
            eventService.updateStatus(event, targetStatus, ex.getMessage());
        }
        messagingTemplate.convertAndSend("/topic/events", EventMapper.toResponse(eventRepository.findByCorrelationId(correlationId).orElseThrow()));
    }

    private void simulateBusinessRules(EventRecord event) {
        if (event.getPayload().toLowerCase().contains("fail")) {
            throw new IllegalStateException("Business validation failed for payload");
        }
    }
}

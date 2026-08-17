package com.example.eventplatform.controller;

import com.example.eventplatform.dto.CreateEventRequest;
import com.example.eventplatform.dto.EventResponse;
import com.example.eventplatform.entity.EventAudit;
import com.example.eventplatform.entity.EventStatus;
import com.example.eventplatform.repository.EventAuditRepository;
import com.example.eventplatform.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
public class EventController {
    private final EventService eventService;
    private final EventAuditRepository auditRepository;

    public EventController(EventService eventService, EventAuditRepository auditRepository) {
        this.eventService = eventService;
        this.auditRepository = auditRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public EventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return EventResponse.from(eventService.create(request));
    }

    @GetMapping
    public List<EventResponse> list(@RequestParam(required = false) EventStatus status) {
        return eventService.list(status).stream().map(EventResponse::from).toList();
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable UUID id) { return EventResponse.from(eventService.get(id)); }

    @GetMapping("/{id}/audit")
    public List<EventAudit> audit(@PathVariable UUID id) { return auditRepository.findByEventIdOrderByCreatedAtAsc(id); }

    @PostMapping("/{id}/retry")
    public EventResponse retry(@PathVariable UUID id) { return EventResponse.from(eventService.retry(id)); }
}

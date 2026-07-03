package com.example.eventplatform.controller;

import com.example.eventplatform.dto.*;
import com.example.eventplatform.entity.EventStatus;
import com.example.eventplatform.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventResponse ingest(@Valid @RequestBody EventRequest request) {
        return eventService.ingest(request);
    }

    @GetMapping("/{id}")
    public EventResponse get(@PathVariable UUID id) {
        return eventService.getById(id);
    }

    @GetMapping
    public List<EventResponse> latestByStatus(@RequestParam EventStatus status) {
        return eventService.latestByStatus(status);
    }

    @PostMapping("/{id}/retry")
    public EventResponse retry(@PathVariable UUID id) {
        return eventService.retry(id);
    }
}

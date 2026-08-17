package com.example.eventplatform.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateEventRequest(
        @NotBlank String correlationId,
        @NotBlank String eventType,
        @NotBlank String payload) {}

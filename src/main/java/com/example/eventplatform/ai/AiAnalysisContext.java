package com.example.eventplatform.ai;

import java.util.List;
import java.util.UUID;

public record AiAnalysisContext(
        UUID eventId,
        String correlationId,
        String eventType,
        String payload,
        String errorMessage,
        int retryCount,
        List<String> statusHistory,
        List<String> auditMessages,
        List<String> retrievedKnowledge
) {}

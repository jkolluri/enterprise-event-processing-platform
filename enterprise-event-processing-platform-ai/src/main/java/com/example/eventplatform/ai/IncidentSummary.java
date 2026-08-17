package com.example.eventplatform.ai;

import java.time.Instant;
import java.util.List;

public record IncidentSummary(
        Instant windowStart,
        Instant windowEnd,
        int failedEventCount,
        List<String> affectedEventTypes,
        String summary,
        String recommendedAction) {}

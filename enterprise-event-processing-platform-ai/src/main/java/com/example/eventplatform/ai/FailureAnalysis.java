package com.example.eventplatform.ai;

public record FailureAnalysis(
        String rootCause,
        ErrorCategory category,
        boolean retryRecommended,
        String remediation,
        double confidence,
        String model,
        boolean llmGenerated) {}

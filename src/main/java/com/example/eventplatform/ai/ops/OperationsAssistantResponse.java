package com.example.eventplatform.ai.ops;

import java.util.List;

public record OperationsAssistantResponse(
        String answer,
        String severity,
        List<String> evidence,
        List<String> recommendedActions,
        boolean humanApprovalRequired,
        String model,
        boolean llmGenerated
) {}

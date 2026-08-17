package com.example.eventplatform.ai.ops;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record OperationsQuestionRequest(
        @NotBlank String question,
        @Min(1) @Max(1440) int minutes
) {
    public OperationsQuestionRequest {
        if (minutes == 0) minutes = 60;
    }
}

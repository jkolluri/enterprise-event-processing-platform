package com.example.eventplatform.ai.rag;

import jakarta.validation.constraints.NotBlank;

public record KnowledgeUpsertRequest(
        @NotBlank String sourceType,
        @NotBlank String sourceRef,
        @NotBlank String title,
        @NotBlank String content
) {}

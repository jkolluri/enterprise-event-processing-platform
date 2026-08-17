package com.example.eventplatform.ai.rag;

import java.time.Instant;
import java.util.UUID;

public record KnowledgeDocument(
        UUID id,
        String sourceType,
        String sourceRef,
        String title,
        String content,
        Double similarity,
        Instant createdAt
) {}

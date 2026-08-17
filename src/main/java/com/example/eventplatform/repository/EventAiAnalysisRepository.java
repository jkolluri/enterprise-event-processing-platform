package com.example.eventplatform.repository;

import com.example.eventplatform.ai.AiAnalysisStatus;
import com.example.eventplatform.entity.EventAiAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EventAiAnalysisRepository extends JpaRepository<EventAiAnalysis, UUID> {
    List<EventAiAnalysis> findByEventIdOrderByCreatedAtDesc(UUID eventId);
    Optional<EventAiAnalysis> findFirstByEventIdOrderByCreatedAtDesc(UUID eventId);
    Optional<EventAiAnalysis> findByIdempotencyKey(String idempotencyKey);
    long countByStatus(AiAnalysisStatus status);
}

package com.example.eventplatform.ai.ops;

import com.example.eventplatform.ai.rag.KnowledgeDocument;
import com.example.eventplatform.ai.rag.RagKnowledgeService;
import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventAiAnalysis;
import com.example.eventplatform.entity.EventAudit;
import com.example.eventplatform.entity.EventStatus;
import com.example.eventplatform.repository.BusinessEventRepository;
import com.example.eventplatform.repository.EventAiAnalysisRepository;
import com.example.eventplatform.repository.EventAuditRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Read-only tools that can safely be exposed to a future AI agent.
 * Mutating actions such as retry remain outside this service and require explicit human action.
 */
@Service
public class EventOperationsService {
    private final BusinessEventRepository events;
    private final EventAuditRepository audits;
    private final EventAiAnalysisRepository analyses;
    private final RagKnowledgeService knowledge;

    public EventOperationsService(BusinessEventRepository events,
                                  EventAuditRepository audits,
                                  EventAiAnalysisRepository analyses,
                                  RagKnowledgeService knowledge) {
        this.events = events;
        this.audits = audits;
        this.analyses = analyses;
        this.knowledge = knowledge;
    }

    public BusinessEvent getEvent(UUID eventId) {
        return events.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
    }

    public List<EventAudit> getEventHistory(UUID eventId) {
        return audits.findByEventIdOrderByCreatedAtAsc(eventId);
    }

    public List<EventAiAnalysis> getAnalyses(UUID eventId) {
        return analyses.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    public List<BusinessEvent> getRecentFailures(int minutes) {
        int safeMinutes = Math.max(1, Math.min(minutes, 1440));
        return events.findByStatusInAndUpdatedAtAfterOrderByUpdatedAtDesc(
                List.of(EventStatus.FAILED, EventStatus.DEAD_LETTER),
                Instant.now().minus(safeMinutes, ChronoUnit.MINUTES));
    }

    public List<KnowledgeDocument> findSimilarKnowledge(String query, int limit) {
        return knowledge.search(query, limit);
    }
}

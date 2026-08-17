package com.example.eventplatform.ai;

import com.example.eventplatform.ai.rag.RagKnowledgeService;
import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventAudit;
import com.example.eventplatform.repository.EventAuditRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AiContextBuilder {
    private final EventAuditRepository auditRepository;
    private final RagKnowledgeService ragKnowledgeService;

    public AiContextBuilder(EventAuditRepository auditRepository, RagKnowledgeService ragKnowledgeService) {
        this.auditRepository = auditRepository;
        this.ragKnowledgeService = ragKnowledgeService;
    }

    public AiAnalysisContext build(BusinessEvent event) {
        List<EventAudit> audits = auditRepository.findByEventIdOrderByCreatedAtAsc(event.getId());
        List<String> statuses = audits.stream().map(a -> a.getStatus().name()).toList();
        List<String> messages = audits.stream()
                .map(a -> a.getCreatedAt() + " | " + a.getStatus() + " | " + safe(a.getMessage()))
                .toList();
        String retrievalQuery = safe(event.getEventType()) + " " + safe(event.getErrorMessage());
        List<String> knowledge = ragKnowledgeService.retrieveContext(retrievalQuery, 3);
        return new AiAnalysisContext(event.getId(), event.getCorrelationId(), event.getEventType(), event.getPayload(),
                event.getErrorMessage(), event.getRetryCount(), statuses, messages, knowledge);
    }

    private String safe(String value) { return value == null ? "" : value; }
}

package com.example.eventplatform.controller;

import com.example.eventplatform.ai.AiAnalysisService;
import com.example.eventplatform.ai.IncidentSummary;
import com.example.eventplatform.ai.IncidentSummaryService;
import com.example.eventplatform.ai.ops.EventOperationsService;
import com.example.eventplatform.ai.ops.OperationsAssistantResponse;
import com.example.eventplatform.ai.ops.OperationsAssistantService;
import com.example.eventplatform.ai.ops.OperationsQuestionRequest;
import com.example.eventplatform.ai.rag.KnowledgeDocument;
import com.example.eventplatform.ai.rag.KnowledgeUpsertRequest;
import com.example.eventplatform.ai.rag.RagKnowledgeService;
import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventAiAnalysis;
import com.example.eventplatform.entity.EventAudit;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiAnalysisService aiAnalysisService;
    private final IncidentSummaryService incidentSummaryService;
    private final RagKnowledgeService knowledgeService;
    private final EventOperationsService operationsService;
    private final OperationsAssistantService operationsAssistantService;

    public AiController(AiAnalysisService aiAnalysisService,
                        IncidentSummaryService incidentSummaryService,
                        RagKnowledgeService knowledgeService,
                        EventOperationsService operationsService,
                        OperationsAssistantService operationsAssistantService) {
        this.aiAnalysisService = aiAnalysisService;
        this.incidentSummaryService = incidentSummaryService;
        this.knowledgeService = knowledgeService;
        this.operationsService = operationsService;
        this.operationsAssistantService = operationsAssistantService;
    }

    @PostMapping("/events/{eventId}/analyze")
    public EventAiAnalysis analyze(@PathVariable UUID eventId,
                                   @RequestParam(defaultValue = "false") boolean force) {
        return aiAnalysisService.analyzeNow(eventId, force);
    }

    @GetMapping("/events/{eventId}/analyses")
    public List<EventAiAnalysis> analyses(@PathVariable UUID eventId) {
        return aiAnalysisService.findForEvent(eventId);
    }

    @GetMapping("/incidents/summary")
    public IncidentSummary incidentSummary(@RequestParam(defaultValue = "60") int minutes) {
        return incidentSummaryService.summarize(minutes);
    }

    @PostMapping("/knowledge")
    public KnowledgeDocument upsertKnowledge(@Valid @RequestBody KnowledgeUpsertRequest request) {
        return knowledgeService.upsert(request);
    }

    @GetMapping("/knowledge")
    public List<KnowledgeDocument> knowledge(@RequestParam(defaultValue = "50") int limit) {
        return knowledgeService.list(limit);
    }

    @PostMapping("/knowledge/reindex")
    public java.util.Map<String, Integer> reindexKnowledge() {
        return java.util.Map.of("updated", knowledgeService.reindexMissingEmbeddings());
    }

    @GetMapping("/knowledge/search")
    public List<KnowledgeDocument> searchKnowledge(@RequestParam String query,
                                                   @RequestParam(defaultValue = "5") int limit) {
        return knowledgeService.search(query, limit);
    }

    @PostMapping("/ops/ask")
    public OperationsAssistantResponse askOperations(@Valid @RequestBody OperationsQuestionRequest request) {
        return operationsAssistantService.ask(request);
    }

    // Safe, read-only operations endpoints suitable for later tool-calling/agent orchestration.
    @GetMapping("/ops/events/{eventId}")
    public BusinessEvent event(@PathVariable UUID eventId) { return operationsService.getEvent(eventId); }

    @GetMapping("/ops/events/{eventId}/history")
    public List<EventAudit> history(@PathVariable UUID eventId) { return operationsService.getEventHistory(eventId); }

    @GetMapping("/ops/failures")
    public List<BusinessEvent> failures(@RequestParam(defaultValue = "60") int minutes) {
        return operationsService.getRecentFailures(minutes);
    }
}

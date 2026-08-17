package com.example.eventplatform.controller;

import com.example.eventplatform.ai.AiAnalysisService;
import com.example.eventplatform.ai.IncidentSummary;
import com.example.eventplatform.ai.IncidentSummaryService;
import com.example.eventplatform.entity.EventAiAnalysis;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiAnalysisService aiAnalysisService;
    private final IncidentSummaryService incidentSummaryService;

    public AiController(AiAnalysisService aiAnalysisService, IncidentSummaryService incidentSummaryService) {
        this.aiAnalysisService = aiAnalysisService;
        this.incidentSummaryService = incidentSummaryService;
    }

    @PostMapping("/events/{eventId}/analyze")
    public EventAiAnalysis analyze(@PathVariable UUID eventId) { return aiAnalysisService.analyzeNow(eventId); }

    @GetMapping("/events/{eventId}/analyses")
    public List<EventAiAnalysis> analyses(@PathVariable UUID eventId) { return aiAnalysisService.findForEvent(eventId); }

    @GetMapping("/incidents/summary")
    public IncidentSummary incidentSummary(@RequestParam(defaultValue = "60") int minutes) {
        return incidentSummaryService.summarize(minutes);
    }
}

package com.example.eventplatform.ai;

import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventAiAnalysis;
import com.example.eventplatform.repository.BusinessEventRepository;
import com.example.eventplatform.repository.EventAiAnalysisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AiAnalysisService {
    private final BusinessEventRepository eventRepository;
    private final EventAiAnalysisRepository analysisRepository;
    private final OpenAiFailureAnalyzer analyzer;

    public AiAnalysisService(BusinessEventRepository eventRepository,
                             EventAiAnalysisRepository analysisRepository,
                             OpenAiFailureAnalyzer analyzer) {
        this.eventRepository = eventRepository;
        this.analysisRepository = analysisRepository;
        this.analyzer = analyzer;
    }

    @Transactional
    public EventAiAnalysis analyzeNow(UUID eventId) {
        BusinessEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        FailureAnalysis result = analyzer.analyze(event);
        EventAiAnalysis entity = new EventAiAnalysis();
        entity.setEventId(eventId);
        entity.setAnalysisType("FAILURE_ANALYSIS");
        entity.setRootCause(result.rootCause());
        entity.setErrorCategory(result.category());
        entity.setRetryRecommended(result.retryRecommended());
        entity.setRemediation(result.remediation());
        entity.setConfidence(result.confidence());
        entity.setModel(result.model());
        entity.setLlmGenerated(result.llmGenerated());
        return analysisRepository.save(entity);
    }

    public List<EventAiAnalysis> findForEvent(UUID eventId) {
        return analysisRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }
}

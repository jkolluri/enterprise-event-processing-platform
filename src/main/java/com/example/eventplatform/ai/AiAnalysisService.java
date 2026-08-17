package com.example.eventplatform.ai;

import com.example.eventplatform.entity.BusinessEvent;
import com.example.eventplatform.entity.EventAiAnalysis;
import com.example.eventplatform.repository.BusinessEventRepository;
import com.example.eventplatform.repository.EventAiAnalysisRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AiAnalysisService {
    private static final String ANALYSIS_TYPE = "FAILURE_ANALYSIS";

    private final BusinessEventRepository eventRepository;
    private final EventAiAnalysisRepository analysisRepository;
    private final OpenAiFailureAnalyzer analyzer;
    private final AiContextBuilder contextBuilder;
    private final Counter duplicateSkips;

    public AiAnalysisService(BusinessEventRepository eventRepository,
                             EventAiAnalysisRepository analysisRepository,
                             OpenAiFailureAnalyzer analyzer,
                             AiContextBuilder contextBuilder,
                             MeterRegistry meterRegistry) {
        this.eventRepository = eventRepository;
        this.analysisRepository = analysisRepository;
        this.analyzer = analyzer;
        this.contextBuilder = contextBuilder;
        this.duplicateSkips = Counter.builder("ai.analysis.duplicate.skips").register(meterRegistry);
    }

    public EventAiAnalysis analyzeNow(UUID eventId) {
        return analyzeNow(eventId, false);
    }

    public EventAiAnalysis analyzeNow(UUID eventId, boolean force) {
        BusinessEvent event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

        String baseKey = eventId + ":" + event.getRetryCount() + ":" + ANALYSIS_TYPE;
        String idempotencyKey = force ? baseKey + ":MANUAL:" + UUID.randomUUID() : baseKey;

        if (!force) {
            var existing = analysisRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                duplicateSkips.increment();
                return existing.get();
            }
        }

        EventAiAnalysis analysis;
        try {
            analysis = createPending(event, idempotencyKey);
        } catch (DataIntegrityViolationException ex) {
            duplicateSkips.increment();
            return analysisRepository.findByIdempotencyKey(idempotencyKey).orElseThrow(() -> ex);
        }
        analysis.setStatus(AiAnalysisStatus.PROCESSING);
        analysis = analysisRepository.save(analysis);

        // Build context and call external AI with no long-running database transaction held open.
        try {
            AiAnalysisContext context = contextBuilder.build(event);
            long started = System.nanoTime();
            FailureAnalysis result = analyzer.analyze(context);
            long latencyMs = (System.nanoTime() - started) / 1_000_000;

            analysis.setRootCause(result.rootCause());
            analysis.setErrorCategory(result.category());
            analysis.setRetryRecommended(result.retryRecommended());
            analysis.setRemediation(result.remediation());
            analysis.setConfidence(result.confidence());
            analysis.setModel(result.model());
            analysis.setLlmGenerated(result.llmGenerated());
            analysis.setInputTokens(result.inputTokens());
            analysis.setOutputTokens(result.outputTokens());
            analysis.setLatencyMs(latencyMs);
            analysis.setStatus(result.llmGenerated() ? AiAnalysisStatus.COMPLETED : AiAnalysisStatus.FALLBACK);
            analysis.setCompletedAt(Instant.now());
            return analysisRepository.save(analysis);
        } catch (RuntimeException ex) {
            analysis.setStatus(AiAnalysisStatus.FAILED);
            analysis.setFailureReason(limit(ex.getMessage(), 2000));
            analysis.setCompletedAt(Instant.now());
            analysisRepository.save(analysis);
            throw ex;
        }
    }

    public List<EventAiAnalysis> findForEvent(UUID eventId) {
        return analysisRepository.findByEventIdOrderByCreatedAtDesc(eventId);
    }

    private EventAiAnalysis createPending(BusinessEvent event, String idempotencyKey) {
        EventAiAnalysis entity = new EventAiAnalysis();
        entity.setEventId(event.getId());
        entity.setAnalysisType(ANALYSIS_TYPE);
        entity.setRetryCountSnapshot(event.getRetryCount());
        entity.setIdempotencyKey(idempotencyKey);
        entity.setStatus(AiAnalysisStatus.PENDING);
return analysisRepository.saveAndFlush(entity);
    }

    private String limit(String value, int max) {
        if (value == null) return "Unknown AI analysis failure";
        return value.length() <= max ? value : value.substring(0, max);
    }
}

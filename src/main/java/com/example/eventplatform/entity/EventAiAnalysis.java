package com.example.eventplatform.entity;

import com.example.eventplatform.ai.AiAnalysisStatus;
import com.example.eventplatform.ai.ErrorCategory;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_ai_analysis",
        uniqueConstraints = @UniqueConstraint(name = "uk_ai_idempotency", columnNames = "idempotency_key"),
        indexes = {
                @Index(name = "idx_ai_event", columnList = "event_id"),
                @Index(name = "idx_ai_category", columnList = "error_category"),
                @Index(name = "idx_ai_status", columnList = "status")
        })
public class EventAiAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "event_id", nullable = false)
    private UUID eventId;
    @Column(name = "analysis_type", nullable = false)
    private String analysisType;
    @Column(name = "idempotency_key", nullable = false, unique = true, length = 200)
    private String idempotencyKey;
    @Column(name = "retry_count_snapshot", nullable = false)
    private int retryCountSnapshot;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AiAnalysisStatus status;
    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;
    @Enumerated(EnumType.STRING)
    @Column(name = "error_category")
    private ErrorCategory errorCategory;
    @Column(name = "retry_recommended")
    private Boolean retryRecommended;
    @Column(columnDefinition = "TEXT")
    private String remediation;
    private Double confidence;
    private String model;
    @Column(name = "llm_generated", nullable = false)
    private boolean llmGenerated;
    @Column(name = "input_tokens")
    private Long inputTokens;
    @Column(name = "output_tokens")
    private Long outputTokens;
    @Column(name = "latency_ms")
    private Long latencyMs;
    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "completed_at")
    private Instant completedAt;

    @PrePersist
    void prePersist() {
        if (status == null) status = AiAnalysisStatus.PENDING;
        if (createdAt == null) createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
    public int getRetryCountSnapshot() { return retryCountSnapshot; }
    public void setRetryCountSnapshot(int retryCountSnapshot) { this.retryCountSnapshot = retryCountSnapshot; }
    public AiAnalysisStatus getStatus() { return status; }
    public void setStatus(AiAnalysisStatus status) { this.status = status; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public ErrorCategory getErrorCategory() { return errorCategory; }
    public void setErrorCategory(ErrorCategory errorCategory) { this.errorCategory = errorCategory; }
    public Boolean getRetryRecommended() { return retryRecommended; }
    public void setRetryRecommended(Boolean retryRecommended) { this.retryRecommended = retryRecommended; }
    public String getRemediation() { return remediation; }
    public void setRemediation(String remediation) { this.remediation = remediation; }
    public Double getConfidence() { return confidence; }
    public void setConfidence(Double confidence) { this.confidence = confidence; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public boolean isLlmGenerated() { return llmGenerated; }
    public void setLlmGenerated(boolean llmGenerated) { this.llmGenerated = llmGenerated; }
    public Long getInputTokens() { return inputTokens; }
    public void setInputTokens(Long inputTokens) { this.inputTokens = inputTokens; }
    public Long getOutputTokens() { return outputTokens; }
    public void setOutputTokens(Long outputTokens) { this.outputTokens = outputTokens; }
    public Long getLatencyMs() { return latencyMs; }
    public void setLatencyMs(Long latencyMs) { this.latencyMs = latencyMs; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
}

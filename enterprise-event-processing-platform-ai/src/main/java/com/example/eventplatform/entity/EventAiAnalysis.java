package com.example.eventplatform.entity;

import com.example.eventplatform.ai.ErrorCategory;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "event_ai_analysis", indexes = {
        @Index(name = "idx_ai_event", columnList = "eventId"),
        @Index(name = "idx_ai_category", columnList = "errorCategory")
})
public class EventAiAnalysis {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(nullable = false)
    private UUID eventId;
    @Column(nullable = false)
    private String analysisType;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String rootCause;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ErrorCategory errorCategory;
    @Column(nullable = false)
    private boolean retryRecommended;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String remediation;
    @Column(nullable = false)
    private double confidence;
    @Column(nullable = false)
    private String model;
    @Column(nullable = false)
    private boolean llmGenerated;
    @Column(nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() { if (createdAt == null) createdAt = Instant.now(); }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public String getAnalysisType() { return analysisType; }
    public void setAnalysisType(String analysisType) { this.analysisType = analysisType; }
    public String getRootCause() { return rootCause; }
    public void setRootCause(String rootCause) { this.rootCause = rootCause; }
    public ErrorCategory getErrorCategory() { return errorCategory; }
    public void setErrorCategory(ErrorCategory errorCategory) { this.errorCategory = errorCategory; }
    public boolean isRetryRecommended() { return retryRecommended; }
    public void setRetryRecommended(boolean retryRecommended) { this.retryRecommended = retryRecommended; }
    public String getRemediation() { return remediation; }
    public void setRemediation(String remediation) { this.remediation = remediation; }
    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public boolean isLlmGenerated() { return llmGenerated; }
    public void setLlmGenerated(boolean llmGenerated) { this.llmGenerated = llmGenerated; }
    public Instant getCreatedAt() { return createdAt; }
}

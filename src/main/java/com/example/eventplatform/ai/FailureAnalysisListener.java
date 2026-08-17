package com.example.eventplatform.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FailureAnalysisListener {
    private static final Logger log = LoggerFactory.getLogger(FailureAnalysisListener.class);
    private final AiAnalysisService aiAnalysisService;

    public FailureAnalysisListener(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFailure(FailureDetectedEvent event) {
        try {
            aiAnalysisService.analyzeNow(event.eventId());
        } catch (RuntimeException ex) {
            log.error("AI analysis failed for event {}", event.eventId(), ex);
        }
    }
}

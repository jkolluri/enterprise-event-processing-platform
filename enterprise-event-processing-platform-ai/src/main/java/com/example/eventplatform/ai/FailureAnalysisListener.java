package com.example.eventplatform.ai;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FailureAnalysisListener {
    private final AiAnalysisService aiAnalysisService;

    public FailureAnalysisListener(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFailure(FailureDetectedEvent event) {
        aiAnalysisService.analyzeNow(event.eventId());
    }
}

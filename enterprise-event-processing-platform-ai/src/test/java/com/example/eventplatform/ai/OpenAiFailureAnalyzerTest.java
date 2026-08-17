package com.example.eventplatform.ai;

import com.example.eventplatform.entity.BusinessEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenAiFailureAnalyzerTest {
    @Test
    void fallbackClassifiesDataQualityFailure() {
        OpenAiFailureAnalyzer analyzer = new OpenAiFailureAnalyzer(new ObjectMapper(), "", "gpt-5-mini", true);
        BusinessEvent event = new BusinessEvent();
        event.setEventType("PAYMENT");
        event.setErrorMessage("accountId is required and must not be null");

        FailureAnalysis result = analyzer.fallback(event);
        assertThat(result.category()).isEqualTo(ErrorCategory.DATA_QUALITY);
        assertThat(result.retryRecommended()).isFalse();
        assertThat(result.llmGenerated()).isFalse();
    }

    @Test
    void fallbackClassifiesTimeoutAsRetryable() {
        OpenAiFailureAnalyzer analyzer = new OpenAiFailureAnalyzer(new ObjectMapper(), "", "gpt-5-mini", true);
        BusinessEvent event = new BusinessEvent();
        event.setErrorMessage("downstream service timeout");

        FailureAnalysis result = analyzer.fallback(event);
        assertThat(result.category()).isEqualTo(ErrorCategory.TRANSIENT);
        assertThat(result.retryRecommended()).isTrue();
    }
}

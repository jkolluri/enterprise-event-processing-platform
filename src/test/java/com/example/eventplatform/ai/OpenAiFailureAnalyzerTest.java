package com.example.eventplatform.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OpenAiFailureAnalyzerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final AiPayloadSanitizer sanitizer = new AiPayloadSanitizer(mapper, 10_000);
    private final OpenAiFailureAnalyzer analyzer = new OpenAiFailureAnalyzer(
            mapper,
            mock(OpenAiRemoteClient.class),
            sanitizer,
            new SimpleMeterRegistry(),
            "",
            "gpt-5-mini",
            true
    );

    @Test
    void fallbackClassifiesDataQualityFailure() {
        AiAnalysisContext context = context("accountId is required and must not be null");
        FailureAnalysis result = analyzer.fallback(context);
        assertThat(result.category()).isEqualTo(ErrorCategory.DATA_QUALITY);
        assertThat(result.retryRecommended()).isFalse();
        assertThat(result.llmGenerated()).isFalse();
    }

    @Test
    void fallbackClassifiesTimeoutAsRetryable() {
        FailureAnalysis result = analyzer.fallback(context("downstream service timeout"));
        assertThat(result.category()).isEqualTo(ErrorCategory.TRANSIENT);
        assertThat(result.retryRecommended()).isTrue();
    }

    private AiAnalysisContext context(String error) {
        return new AiAnalysisContext(UUID.randomUUID(), "corr-1", "PAYMENT", "{}", error, 0,
                List.of("RECEIVED", "FAILED"), List.of(error), List.of());
    }
}

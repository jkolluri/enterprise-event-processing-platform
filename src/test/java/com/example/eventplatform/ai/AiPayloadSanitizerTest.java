package com.example.eventplatform.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiPayloadSanitizerTest {
    private final AiPayloadSanitizer sanitizer = new AiPayloadSanitizer(new ObjectMapper(), 1000);

    @Test
    void redactsSensitiveJsonFieldsAndEmail() {
        String result = sanitizer.sanitize("{\"accountNumber\":\"1234567890123\",\"email\":\"user@example.com\",\"amount\":250}");
        assertThat(result).contains("[REDACTED]");
        assertThat(result).doesNotContain("1234567890123", "user@example.com");
        assertThat(result).contains("250");
    }

    @Test
    void truncatesLargePayloads() {
        String result = sanitizer.sanitize("x".repeat(1500));
        assertThat(result).endsWith("...[TRUNCATED]");
    }
}

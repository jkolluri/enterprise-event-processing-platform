package com.example.eventplatform.ai.rag;

import com.example.eventplatform.ai.OpenAiRemoteClient;
import com.fasterxml.jackson.databind.JsonNode;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class OpenAiEmbeddingService {
    private final OpenAiRemoteClient client;
    private final String apiKey;
    private final String model;
    private final boolean enabled;
    private final Counter requests;
    private final Counter failures;

    public OpenAiEmbeddingService(OpenAiRemoteClient client,
                                  MeterRegistry meterRegistry,
                                  @Value("${app.ai.api-key:}") String apiKey,
                                  @Value("${app.ai.embedding-model:text-embedding-3-small}") String model,
                                  @Value("${app.ai.rag.enabled:true}") boolean enabled) {
        this.client = client;
        this.apiKey = apiKey;
        this.model = model;
        this.enabled = enabled;
        this.requests = Counter.builder("ai.embedding.requests").register(meterRegistry);
        this.failures = Counter.builder("ai.embedding.failures").register(meterRegistry);
    }

    public Optional<List<Double>> embed(String input) {
        if (!enabled || !StringUtils.hasText(apiKey) || !StringUtils.hasText(input)) return Optional.empty();
        requests.increment();
        try {
            JsonNode response = client.createEmbedding(Map.of("model", model, "input", input));
            JsonNode embedding = response.path("data").path(0).path("embedding");
            if (!embedding.isArray()) return Optional.empty();
            List<Double> vector = new ArrayList<>(embedding.size());
            embedding.forEach(value -> vector.add(value.asDouble()));
            return Optional.of(vector);
        } catch (Exception ex) {
            failures.increment();
            return Optional.empty();
        }
    }

    public String model() { return model; }
}

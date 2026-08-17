package com.example.eventplatform.ai;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OpenAiRemoteClient {
    private final RestClient restClient;
    private final String apiKey;

    public OpenAiRemoteClient(@Value("${app.ai.api-key:}") String apiKey,
                              @Value("${app.ai.connect-timeout-ms:3000}") int connectTimeoutMs,
                              @Value("${app.ai.read-timeout-ms:15000}") int readTimeoutMs) {
        this.apiKey = apiKey;
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeoutMs);
        requestFactory.setReadTimeout(readTimeoutMs);
        this.restClient = RestClient.builder()
                .baseUrl("https://api.openai.com")
                .requestFactory(requestFactory)
                .build();
    }

    @Retry(name = "openai")
    @CircuitBreaker(name = "openai")
    public JsonNode createResponse(Map<String, Object> body) {
        return restClient.post()
                .uri("/v1/responses")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }

    @Retry(name = "openai")
    @CircuitBreaker(name = "openai")
    public JsonNode createEmbedding(Map<String, Object> body) {
        return restClient.post()
                .uri("/v1/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(JsonNode.class);
    }
}

package com.example.eventplatform.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class AiPayloadSanitizer {
    private static final String REDACTED = "[REDACTED]";
    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwd", "secret", "token", "accesstoken", "refreshtoken", "authorization",
            "apikey", "api_key", "ssn", "socialsecuritynumber", "accountnumber", "routingnumber",
            "cardnumber", "cvv", "email", "phone", "phonenumber", "customername", "firstname", "lastname"
    );
    private static final Pattern EMAIL = Pattern.compile("(?i)\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}\\b");
    private static final Pattern SSN = Pattern.compile("\\b\\d{3}-?\\d{2}-?\\d{4}\\b");
    private static final Pattern CARD = Pattern.compile("\\b(?:\\d[ -]*?){13,19}\\b");
    private final ObjectMapper mapper;
    private final int maxLength;

    public AiPayloadSanitizer(ObjectMapper mapper, @Value("${app.ai.max-payload-chars:10000}") int maxLength) {
        this.mapper = mapper;
        this.maxLength = Math.max(1000, maxLength);
    }

    public String sanitize(String value) {
        if (value == null || value.isBlank()) return "";
        String sanitized = tryJsonRedaction(value);
        sanitized = EMAIL.matcher(sanitized).replaceAll(REDACTED);
        sanitized = SSN.matcher(sanitized).replaceAll(REDACTED);
        sanitized = CARD.matcher(sanitized).replaceAll(REDACTED);
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength) + "...[TRUNCATED]";
        }
        return sanitized;
    }

    private String tryJsonRedaction(String value) {
        try {
            JsonNode root = mapper.readTree(value);
            redact(root);
            return mapper.writeValueAsString(root);
        } catch (JsonProcessingException ignored) {
            return value;
        }
    }

    private void redact(JsonNode node) {
        if (node instanceof ObjectNode objectNode) {
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String normalized = entry.getKey().replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
                if (SENSITIVE_KEYS.contains(normalized)) objectNode.put(entry.getKey(), REDACTED);
                else redact(entry.getValue());
            }
        } else if (node.isArray()) {
            node.forEach(this::redact);
        }
    }
}

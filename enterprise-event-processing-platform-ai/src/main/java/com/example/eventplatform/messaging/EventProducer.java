package com.example.eventplatform.messaging;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class EventProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;
    public EventProducer(KafkaTemplate<String, String> kafkaTemplate,
                         @Value("${app.kafka.event-topic:business-events}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }
    public void publish(UUID eventId) { kafkaTemplate.send(topic, eventId.toString(), eventId.toString()); }
}

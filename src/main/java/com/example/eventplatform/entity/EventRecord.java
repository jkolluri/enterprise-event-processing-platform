package com.example.eventplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "event_records", indexes = {
        @Index(name = "idx_event_correlation_id", columnList = "correlationId"),
        @Index(name = "idx_event_status", columnList = "status")
})
public class EventRecord {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String correlationId;
    @Column(nullable = false)
    private String eventType;
    @Lob
    @Column(nullable = false)
    private String payload;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;
    private int retryCount;
    private String failureReason;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}

package com.example.eventplatform.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "event_audit")
public class EventAudit {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID eventId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus oldStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus newStatus;
    private String message;
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        createdAt = Instant.now();
    }
}

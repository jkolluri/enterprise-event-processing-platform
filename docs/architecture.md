# Architecture

```mermaid
flowchart LR
    Client[REST Client / UI] --> API[Spring Boot REST API]
    API --> DB[(PostgreSQL)]
    API --> K[Kafka Topic]
    K --> Worker[Event Processor]
    Worker --> DB
    Worker --> WS[WebSocket Broker]
    WS --> Dashboard[Live Dashboard]
    API --> Redis[(Redis Cache)]
```

## Design Highlights

- REST API accepts event ingestion requests.
- Kafka decouples ingestion from processing.
- PostgreSQL stores event records and audit history.
- WebSocket/STOMP broadcasts live event status changes.
- Retry API supports operational recovery.
- Dead-letter status captures events that fail repeatedly.

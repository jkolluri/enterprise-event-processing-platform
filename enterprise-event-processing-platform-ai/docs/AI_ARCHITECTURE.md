# AI Architecture

The AI layer is advisory and isolated from deterministic event processing.

```mermaid
flowchart LR
  API[REST API] --> DB[(PostgreSQL)]
  API --> K[Kafka]
  K --> P[Event Processor]
  P --> DB
  P --> WS[WebSocket / STOMP]
  P -->|FAILED / DEAD_LETTER| A[Async AI Analysis]
  A --> LLM[OpenAI Responses API]
  A --> F[Rule-based fallback]
  LLM --> AD[(event_ai_analysis)]
  F --> AD
  AD --> AIAPI[AI Operations API]
```

## Guardrails

- AI does not modify inbound payloads.
- AI cannot execute retry automatically.
- Retry advice is stored separately from transactional event state.
- Event payload/error text are explicitly treated as untrusted data in the LLM prompt.
- `store=false` is sent to the model API.
- If AI is disabled, unavailable, or unconfigured, a deterministic classifier provides a safe fallback.
- The Kafka consumer delegates model work to a separate bounded executor so model latency does not block the consumer thread.

## Transaction boundary

Failure analysis is triggered with `@TransactionalEventListener(AFTER_COMMIT)` so the AI worker only reads committed event/error state.

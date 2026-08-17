# Enterprise Event Processing Platform — AI Operations Edition

A Java 21 / Spring Boot platform for event ingestion, Kafka processing, audit tracking, retries, WebSocket updates, and AI-assisted operations.

## What the AI layer does

When an event fails, the application commits the event failure first and then asynchronously starts AI analysis. The AI path never blocks or owns the Kafka processing transaction.

The analysis produces structured fields:

- root cause
- error category
- retry recommendation
- remediation
- confidence
- model
- input/output token usage
- latency
- lifecycle status (`PENDING`, `PROCESSING`, `COMPLETED`, `FALLBACK`, `FAILED`)

If OpenAI is disabled/unavailable, the platform uses a deterministic rule-based fallback. AI never automatically retries, replays, reprocesses, restarts, or modifies production state.

## AI safety and production controls

- Sensitive JSON keys, emails, SSNs, and card-like values are redacted before model calls.
- Payloads are capped with `AI_MAX_PAYLOAD_CHARS`.
- Event payload/error/knowledge content is treated as untrusted data rather than instructions.
- Structured Outputs (JSON Schema) are used for failure analysis and operations-assistant responses.
- OpenAI calls have connection/read timeouts, bounded retry, and a circuit breaker.
- Duplicate automatic analyses are prevented with an idempotency key based on event ID + retry count + analysis type.
- AI calls are outside long database transactions.
- LLM request count, failure count, fallback count, duplicate skips, latency, embedding calls, and token usage are published through Micrometer/Prometheus.

## RAG / operational knowledge

The project uses PostgreSQL + pgvector for operational knowledge such as runbooks, known errors, and historical incident notes.

Flow:

```text
Failed event
    -> sanitized failure context
    -> embedding / pgvector retrieval
    -> top runbooks/incidents
    -> structured LLM analysis
    -> persisted EventAiAnalysis
```

Seeded runbooks cover Kafka connectivity, downstream timeouts, and data-quality failures. If no embedding is available, retrieval safely degrades to keyword matching. After configuring `OPENAI_API_KEY`, call the reindex endpoint to generate embeddings for documents that do not yet have vectors.

## Read-only operations assistant

`POST /api/ai/ops/ask` investigates recent failures using event state, stored AI analyses, and retrieved runbooks. It is intentionally read-only. Mutating actions remain in normal application APIs and require explicit human action.

Example:

```json
{
  "question": "Why are payment events failing in the last 30 minutes?",
  "minutes": 30
}
```

## Main APIs

```text
POST /api/events
GET  /api/events
GET  /api/events/{eventId}
GET  /api/events/{eventId}/audit
POST /api/events/{eventId}/retry

POST /api/ai/events/{eventId}/analyze?force=false
GET  /api/ai/events/{eventId}/analyses
GET  /api/ai/incidents/summary?minutes=60

POST /api/ai/knowledge
GET  /api/ai/knowledge
GET  /api/ai/knowledge/search?query=timeout&limit=5
POST /api/ai/knowledge/reindex

POST /api/ai/ops/ask
GET  /api/ai/ops/failures?minutes=60
GET  /api/ai/ops/events/{eventId}
GET  /api/ai/ops/events/{eventId}/history
```

## Stack

- Java 21
- Spring Boot 3.3.13
- Spring Web / Security / JPA / Validation / WebSocket
- Apache Kafka
- PostgreSQL 16 + pgvector
- Redis
- OpenAI Responses API + Embeddings API
- Resilience4j
- Micrometer + Prometheus
- Flyway
- Docker Compose

## Start infrastructure

```bash
docker compose up -d
```

This starts PostgreSQL/pgvector, Redis, Kafka/Zookeeper, and Prometheus.

## Configure AI

```bash
export OPENAI_API_KEY="your-key"
export OPENAI_MODEL="gpt-5-mini"
export OPENAI_EMBEDDING_MODEL="text-embedding-3-small"
```

The application also works without an API key; failure analysis then uses deterministic fallback rules.

## Run

```bash
mvn clean test
mvn spring-boot:run
```

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

Prometheus metrics endpoint (JWT-protected by default):

```text
http://localhost:8080/actuator/prometheus
```

Prometheus UI:

```text
http://localhost:9090
```

Demo credentials:

```text
username: admin
password: admin123
```

For real deployment, replace the demo account and secret with your identity provider / secret manager.

## Useful AI metrics

Micrometer normalizes dotted metric names for the Prometheus endpoint. Important meters include:

```text
ai.analysis.requests
ai.analysis.failures
ai.analysis.fallbacks
ai.analysis.duplicate.skips
ai.analysis.latency
ai.tokens.input
ai.tokens.output
ai.embedding.requests
ai.embedding.failures
```

## AI analysis lifecycle

```text
Event FAILED / DEAD_LETTER
        |
        | transaction commits
        v
AFTER_COMMIT event listener
        |
        v
PENDING -> PROCESSING
        |
        +-> RAG retrieval
        +-> sanitized model request
        |
        +-> COMPLETED (LLM result)
        |
        +-> FALLBACK (deterministic result)
        |
        +-> FAILED (unexpected application error)
```

## Human-in-the-loop rule

AI output is advisory. A retry recommendation is never an execution command. `POST /api/events/{eventId}/retry` remains a separate explicit action.

## Agent Skill: Event Ops Investigator

This repository includes a repo-scoped Agent Skill at:

```text
.agents/skills/event-ops/
```

Agents that support the Agent Skills format can discover the `event-ops` skill from the repository. In Codex, ask to use `$event-ops` (or ask it to investigate event failures) while working in this repo.

The skill is intentionally **read-only for operational state**: it can inspect failures, event history, AI analyses, incident summaries, and RAG runbooks, and it can request advisory AI analysis. It never executes the event retry endpoint.

You can also run the bundled helper directly:

```bash
.agents/skills/event-ops/scripts/event-ops.sh failures 60
.agents/skills/event-ops/scripts/event-ops.sh incident 60
.agents/skills/event-ops/scripts/event-ops.sh ask "Why are payment events failing?" 30
.agents/skills/event-ops/scripts/event-ops.sh knowledge "Kafka broker disconnected" 5
```

Optional configuration:

```bash
export EVENT_PLATFORM_URL=http://localhost:8080
export EVENT_PLATFORM_USER=admin
export EVENT_PLATFORM_PASSWORD=admin123
```

For real deployments, provide credentials through your secret-management mechanism rather than committing them to Git.

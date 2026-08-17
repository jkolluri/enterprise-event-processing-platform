# AI Architecture

## Design principles

1. Core event processing must remain deterministic and available when AI is unavailable.
2. AI network calls never run inside the Kafka event transaction.
3. AI output is advisory, structured, persisted, observable, and auditable.
4. Event payloads are untrusted and are sanitized before leaving the application.
5. Retrieval grounds model output in operational knowledge.
6. Operations-assistant access is read-only; production mutations require human approval.

## Failure path

```text
Kafka -> EventConsumer -> FAILED/DEAD_LETTER -> DB commit
                                           -> AFTER_COMMIT listener
                                           -> idempotency check
                                           -> PENDING/PROCESSING
                                           -> audit history + RAG
                                           -> sanitize
                                           -> OpenAI + resilience
                                           -> COMPLETED/FALLBACK/FAILED
```

## RAG

`ai_knowledge_document` stores runbooks and incident notes. Embeddings use `text-embedding-3-small` by default and pgvector cosine distance. Null embeddings are allowed so knowledge can be added even while OpenAI is unavailable; keyword retrieval acts as a fallback and `/api/ai/knowledge/reindex` backfills missing vectors later.

## Observability

Metrics capture model request/failure/fallback counts, latency, input/output token usage, embedding requests/failures, and duplicate analysis skips. Resilience4j exposes circuit-breaker health/events through Actuator.

## Security

The sanitizer recursively redacts sensitive JSON keys and common textual PII patterns. Prompt instructions explicitly mark payloads, errors, and retrieved knowledge as untrusted evidence. Requests use `store=false`.

## Human approval boundary

The operations assistant can inspect failures, audit history, prior analyses, and knowledge documents. It has no retry/replay/reprocess/restart/configuration mutation tool. Remediation must be approved and executed through explicit application APIs or operator workflows.

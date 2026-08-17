# AI Upgrade Changelog

## 0.2.0

- Upgraded Spring Boot 3.3.5 -> 3.3.13.
- Removed long database transaction around external model calls.
- Added AI lifecycle: PENDING, PROCESSING, COMPLETED, FALLBACK, FAILED.
- Added idempotency per event/retry/analysis type.
- Added PII/sensitive payload redaction and prompt-size limits.
- Added separate trusted instructions vs untrusted event data.
- Added OpenAI request timeouts, bounded retry, and circuit breaker.
- Added Micrometer/Prometheus AI request, latency, token, fallback, failure, embedding, and duplicate metrics.
- Added audit history and status history to failure-analysis context.
- Added PostgreSQL pgvector RAG store and Flyway migrations.
- Added runbook seeding, vector search, keyword fallback, and embedding reindex endpoint.
- Added read-only operations assistant and safe investigation endpoints.
- Kept retry/remediation actions explicitly human-controlled.
- Added migration support for existing `event_ai_analysis` tables.
- Added sanitizer and fallback-classification tests.

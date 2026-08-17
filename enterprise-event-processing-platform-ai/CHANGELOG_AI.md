# AI Upgrade Changelog

- Added persisted `event_ai_analysis` entity/repository.
- Added asynchronous failure analysis on `FAILED` and `DEAD_LETTER` events.
- Added OpenAI Responses API structured-output integration.
- Added deterministic fallback analysis when API key/model is unavailable.
- Added retry-safety classification and remediation guidance.
- Added incident-summary API.
- Preserved manual retry as a human-controlled action.
- Added bounded AI executor to avoid blocking Kafka consumer work.
- Added prompt-injection boundary: event data is treated as untrusted input.
- Added AI unit tests and architecture documentation.
- AI analysis starts only after the failed event transaction commits, preventing stale reads.

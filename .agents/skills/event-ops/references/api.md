# Event Operations API Reference

Base path: `/api`

Authentication:

- `POST /api/auth/login`
- Request: `{ "username": "...", "password": "..." }`
- Use returned JWT as `Authorization: Bearer <token>`.

Read-only operations used by the skill:

- `GET /api/ai/ops/failures?minutes=N`
- `GET /api/ai/ops/events/{eventId}`
- `GET /api/ai/ops/events/{eventId}/history`
- `GET /api/ai/events/{eventId}/analyses`
- `GET /api/ai/incidents/summary?minutes=N`
- `GET /api/ai/knowledge/search?query=...&limit=N`
- `POST /api/ai/ops/ask`

Advisory analysis request:

- `POST /api/ai/events/{eventId}/analyze?force=false`

Explicitly excluded from this skill:

- `POST /api/events/{eventId}/retry`
- Any endpoint that mutates event state or bypasses approval.

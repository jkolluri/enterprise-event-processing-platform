# API Examples

## Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## Ingest Event
```bash
curl -X POST http://localhost:8080/api/events \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"correlationId":"TXN-1001","eventType":"PAYMENT","payload":"{\"amount\":250}"}'
```

## Search Latest Failed Events
```bash
curl -X GET "http://localhost:8080/api/events?status=FAILED" \
  -H "Authorization: Bearer <token>"
```

## Retry Event
```bash
curl -X POST http://localhost:8080/api/events/<event-id>/retry \
  -H "Authorization: Bearer <token>"
```

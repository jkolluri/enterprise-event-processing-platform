INSERT INTO ai_knowledge_document (id, source_type, source_ref, title, content)
VALUES
('00000000-0000-0000-0000-000000000101', 'RUNBOOK', 'KAFKA-001', 'Kafka broker connectivity failure',
 'Symptoms include broker disconnected, connection refused, DNS failure, or repeated bootstrap broker warnings. Verify broker health, advertised listeners, DNS/network connectivity and bootstrap server configuration before retrying. Do not change event payloads for infrastructure-only failures.'),
('00000000-0000-0000-0000-000000000102', 'RUNBOOK', 'DOWNSTREAM-001', 'Downstream timeout handling',
 'For transient downstream timeouts, verify dependency health and recovery. Retry only with bounded exponential backoff and a maximum retry count. If repeated timeouts persist, stop retries and escalate the dependency incident.'),
('00000000-0000-0000-0000-000000000103', 'RUNBOOK', 'DATA-001', 'Invalid or incomplete event payload',
 'Missing required fields, malformed values, null identifiers and validation failures are data-quality problems. Correct or republish the source payload before retrying. Blind retry is not recommended because the same payload will fail again.')
ON CONFLICT (source_type, source_ref) DO NOTHING;

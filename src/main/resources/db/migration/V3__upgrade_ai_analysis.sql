DO $$
BEGIN
    IF to_regclass('public.event_ai_analysis') IS NOT NULL THEN
        ALTER TABLE event_ai_analysis ALTER COLUMN root_cause DROP NOT NULL;
        ALTER TABLE event_ai_analysis ALTER COLUMN error_category DROP NOT NULL;
        ALTER TABLE event_ai_analysis ALTER COLUMN retry_recommended DROP NOT NULL;
        ALTER TABLE event_ai_analysis ALTER COLUMN remediation DROP NOT NULL;
        ALTER TABLE event_ai_analysis ALTER COLUMN confidence DROP NOT NULL;
        ALTER TABLE event_ai_analysis ALTER COLUMN model DROP NOT NULL;

        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS idempotency_key VARCHAR(200);
        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS retry_count_snapshot INTEGER NOT NULL DEFAULT 0;
        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS status VARCHAR(40) NOT NULL DEFAULT 'COMPLETED';
        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS input_tokens BIGINT;
        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS output_tokens BIGINT;
        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS latency_ms BIGINT;
        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS failure_reason TEXT;
        ALTER TABLE event_ai_analysis ADD COLUMN IF NOT EXISTS completed_at TIMESTAMPTZ;

        UPDATE event_ai_analysis
        SET idempotency_key = event_id::text || ':' || retry_count_snapshot::text || ':' || analysis_type || ':LEGACY:' || id::text
        WHERE idempotency_key IS NULL;
        ALTER TABLE event_ai_analysis ALTER COLUMN idempotency_key SET NOT NULL;

        IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uk_ai_idempotency') THEN
            ALTER TABLE event_ai_analysis ADD CONSTRAINT uk_ai_idempotency UNIQUE (idempotency_key);
        END IF;
    END IF;
END $$;

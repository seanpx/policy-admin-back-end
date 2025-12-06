-- Flyway V4: audit event table for business/security logging
CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS audit_event (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    occurred_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    actor_type VARCHAR(32),
    actor_id VARCHAR(128),
    action VARCHAR(64) NOT NULL,
    entity_type VARCHAR(64),
    entity_id VARCHAR(128),
    correlation_id VARCHAR(128),
    request_ip VARCHAR(64),
    metadata_json JSONB
);

CREATE INDEX IF NOT EXISTS idx_audit_event_occurred_at ON audit_event (occurred_at);
CREATE INDEX IF NOT EXISTS idx_audit_event_action ON audit_event (action);

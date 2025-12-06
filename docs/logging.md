# Logging & Auditing Plan

Goal: Make the first frontend/backend calls observable and auditable while keeping PII safe.

## Principles
- Structured JSON logs (Logback with logstash encoder) and consistent field names.
- Correlation ID on every request: read X-Correlation-ID or traceparent, generate UUID when missing, store in MDC; include authenticated username/tenant when present.
- Minimal PII: log ids/status only; never log credentials, tokens, or full bodies; mask or hash sensitive external ids.
- Log once per failure; use levels INFO (state changes), WARN (odd but handled), ERROR (failures); DEBUG only locally.
- Rotate log files and set verbosity per environment.

## Log streams to produce
- Access/request: method, path, status, durationMs, bytesSent, correlationId, userId (if any), clientApp, remoteIp (where policy allows).
- Application/service: domain steps, validations, external calls, retries; include contextual ids like policyId/clientId.
- Domain audit events: append-only records of business actions (who, what, when, correlationId) persisted to DB; avoid PII payloads.
- Security events: auth failures, access denials, admin/config changes.

## Implementation steps (MVP)
1) Add dependency net.logstash.logback:logstash-logback-encoder.
2) Create logback-spring.xml with console JSON encoder (and rolling file appender for non-dev). Fields: timestamp, level, logger, thread, correlationId, userId, method, path, status, durationMs, client, exception.
3) Add OncePerRequestFilter for correlation: reuse incoming header X-Correlation-ID or traceparent; generate UUID when absent; put correlationId/user/tenant into MDC; clear MDC in finally.
4) Add access log filter/interceptor that times requests and logs at INFO on completion; allow sampling if traffic grows.
5) Add @ControllerAdvice to translate exceptions to client-safe responses and log once at ERROR with context (correlationId + domain ids).
6) Set application.yml logging levels: default INFO; com.policyadmin=INFO; org.springframework=INFO; allow dev override to DEBUG.
7) Add audit model/service and Flyway migration for audit_event table: id (uuid), occurred_at, actor_type, actor_id, action, entity_type, entity_id, correlation_id, request_ip, metadata_json; service provides append method.
8) Instrument first flows: client KYC + creation success/fail, authentication failures, access denials.

## PII and redaction rules
- Log identifiers and statuses only; avoid names, emails, phone numbers, full payloads, secrets.
- Mask sensitive values (e.g., last4) or hash before logging when they must be referenced.
- Keep full stack traces in logs only; return sanitized messages to callers.

## Rotation and retention (suggested)
- Dev: console JSON only; optional rolling file 10MB x 10.
- Test/stage/prod: rolling file + shipping to log aggregator when available; retain app logs 14-30d, audit events per compliance (e.g., 1y+).
- Add startup check/warning if log destination is not writable.

## Future
- Add OpenTelemetry tracing/export, sampling for noisy endpoints, log-based alerts on spikes in 5xx/auth failures, dashboards for latency/error rates.

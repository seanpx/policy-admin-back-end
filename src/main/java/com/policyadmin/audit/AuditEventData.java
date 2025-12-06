package com.policyadmin.audit;

import java.util.Map;

public record AuditEventData(
        String action,
        String entityType,
        String entityId,
        String actorType,
        String actorId,
        String correlationId,
        String requestIp,
        Map<String, Object> metadata
) {
}

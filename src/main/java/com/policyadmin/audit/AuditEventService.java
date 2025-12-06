package com.policyadmin.audit;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditEventService {

    private final AuditEventRepository repository;

    public AuditEventService(AuditEventRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void record(AuditEventData data) {
        AuditEvent event = new AuditEvent();
        event.setOccurredAt(Instant.now());
        event.setAction(data.action());
        event.setEntityType(data.entityType());
        event.setEntityId(data.entityId());
        event.setActorType(data.actorType());
        event.setActorId(data.actorId());
        event.setCorrelationId(data.correlationId());
        event.setRequestIp(data.requestIp());
        event.setMetadataJson(data.metadata());

        repository.save(event);
    }
}

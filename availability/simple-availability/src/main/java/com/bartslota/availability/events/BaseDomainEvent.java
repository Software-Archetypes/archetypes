package com.bartslota.availability.events;

import java.time.Instant;
import java.util.UUID;

public abstract class BaseDomainEvent implements DomainEvent {

    private final UUID id;
    private final Instant occurredAt;

    public BaseDomainEvent() {
        this.id = UUID.randomUUID();
        this.occurredAt = Instant.now(); //Clock should be used here
    }

    BaseDomainEvent(UUID id, Instant occurredAt) {
        this.id = id;
        this.occurredAt = occurredAt;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public Instant getOccurredAt() {
        return occurredAt;
    }
}

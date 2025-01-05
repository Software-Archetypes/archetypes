package com.softwarearchetypes.party.events;

import java.time.Instant;
import java.util.UUID;

//TODO: apply IDs with id generator and clock generator
public interface DomainEvent {

    UUID id();

    Instant occurredAt();

    String type();
}

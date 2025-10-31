package com.softwarearchetypes.common.events;

import java.time.Instant;
import java.util.UUID;

public interface PublishedEvent {

    UUID id();

    String type();

    Instant occurredAt();

}

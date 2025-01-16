package com.softwarearchetypes.availability.application

import com.softwarearchetypes.availability.events.DomainEvent
import com.softwarearchetypes.availability.events.DomainEventsPublisher

class InMemoryDomainEventPublisher implements DomainEventsPublisher {

    List<DomainEvent> events = []

    @Override
    void publish(DomainEvent domainEvent) {
        events << domainEvent
    }

    void cleanup() {
        events.clear()
    }

    boolean thereWasAnEventFulfilling(Closure<Boolean> condition) {
        events.find(condition).any()
    }
}

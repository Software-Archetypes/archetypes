package com.bartslota.availability.application

import com.bartslota.availability.events.DomainEvent
import com.bartslota.availability.events.DomainEventsPublisher

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

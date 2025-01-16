package com.softwarearchetypes.availability.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.softwarearchetypes.availability.events.DomainEvent;
import com.softwarearchetypes.availability.events.DomainEventsPublisher;

@Component
class SpringEventPublisher implements DomainEventsPublisher {

    private final ApplicationEventPublisher eventPublisher;

    SpringEventPublisher(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @Override
    public void publish(DomainEvent domainEvent) {
        eventPublisher.publishEvent(domainEvent);
    }
}

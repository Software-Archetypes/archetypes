package com.bartslota.availability.infrastructure.events;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.bartslota.availability.events.DomainEvent;
import com.bartslota.availability.events.DomainEventsPublisher;

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

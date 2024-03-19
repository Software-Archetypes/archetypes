package com.bartslota.availability.events;

public interface DomainEventsPublisher {

    void publish(DomainEvent domainEvent);
}

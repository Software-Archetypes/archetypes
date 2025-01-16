package com.softwarearchetypes.availability.events;

public interface DomainEventsPublisher {

    void publish(DomainEvent domainEvent);
}

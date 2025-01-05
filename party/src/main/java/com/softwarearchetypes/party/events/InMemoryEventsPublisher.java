package com.softwarearchetypes.party.events;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InMemoryEventsPublisher implements EventPublisher {

    private Set<InMemoryEventObserver> observers = new HashSet<>();

    @Override
    public void publish(PublishedEvent event) {
        observers.forEach(it -> it.handle(event));
    }

    @Override
    public void publish(List<PublishedEvent> events) {
        events.forEach(this::publish);
    }

    public void register(InMemoryEventObserver observer) {
        observers.add(observer);
    }

    public interface InMemoryEventObserver {

        void handle(PublishedEvent event);

    }
}

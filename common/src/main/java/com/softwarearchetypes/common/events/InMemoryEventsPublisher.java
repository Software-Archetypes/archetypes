package com.softwarearchetypes.common.events;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InMemoryEventsPublisher implements EventPublisher {

    private final Set<EventHandler> observers = new HashSet<>();

    @Override
    public void publish(PublishedEvent event) {
        observers.forEach(it -> {
            if (it.supports(event)) {
                it.handle(event);
            }
        });
    }

    @Override
    public void publish(List<? extends PublishedEvent> events) {
        events.forEach(this::publish);
    }

    @Override
    public void register(EventHandler eventHandler) {
        observers.add(eventHandler);
    }

}

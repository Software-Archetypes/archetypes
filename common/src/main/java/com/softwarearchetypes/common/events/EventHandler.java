package com.softwarearchetypes.common.events;

public interface EventHandler {

    boolean supports(PublishedEvent event);

    void handle(PublishedEvent event);

}

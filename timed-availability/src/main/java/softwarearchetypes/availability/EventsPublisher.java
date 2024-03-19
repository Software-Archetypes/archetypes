package softwarearchetypes.availability;


public interface EventsPublisher {
    //remember about transactions scope
    void publish(PublishedEvent event);
}



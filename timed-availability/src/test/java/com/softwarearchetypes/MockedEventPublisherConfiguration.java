package com.softwarearchetypes;

import com.softwarearchetypes.availability.EventsPublisher;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

@TestConfiguration(proxyBeanMethods = false)
public class MockedEventPublisherConfiguration {

    @MockBean
    EventsPublisher eventsPublisher;

}

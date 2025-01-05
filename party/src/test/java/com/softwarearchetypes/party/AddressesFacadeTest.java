package com.softwarearchetypes.party;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.party.events.GeoAddressDefined;
import com.softwarearchetypes.party.events.GeoAddressRemoved;
import com.softwarearchetypes.party.events.GeoAddressUpdated;
import com.softwarearchetypes.party.events.InMemoryEventsPublisher;
import com.softwarearchetypes.party.events.PublishedEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressesFacadeTest {

    private InMemoryEventsPublisher eventPublisher = new InMemoryEventsPublisher();
    private InMemoryAddressesRepository repository = new InMemoryAddressesRepository();
    private AddressesFacade addressesFacade = new AddressesFacade(repository, eventPublisher);

    private AddressesTestEventListener testEventListener = new AddressesTestEventListener(eventPublisher);

    @Test
    void canAddAddressToParty() {
        //given
        PartyId partyId = PartyId.random();
        Address address = GeoAddressFixture.someGeoAddressFor(partyId);

        //when
        addressesFacade.addOrUpdate(partyId, address);

        //then
        Addresses addresses = addressesFacade.findFor(partyId).get();
        assertEquals(Set.of(address), addresses.asSet());
    }

    @Test
    void addressCreatedEventIsEmittedWhenAddressIsAddedToParty() {
        //given
        PartyId partyId = PartyId.random();
        Address address = GeoAddressFixture.someGeoAddressFor(partyId);

        //and
        GeoAddressDefined expectedEvent = (GeoAddressDefined) address.toAddressDefinitionSucceededEvent();

        //when
        addressesFacade.addOrUpdate(partyId, address);

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    @Test
    void canUpdateExistingAddressOfParty() {
        //given
        PartyId partyId = PartyId.random();
        Address address = thereIsSomeGeoAddressFor(partyId);

        //and
        Address newAddress = GeoAddressFixture.someGeoAddressWith(address.id(), partyId);

        //when
        addressesFacade.addOrUpdate(partyId, newAddress);

        //then
        Addresses addresses = addressesFacade.findFor(partyId).get();
        assertEquals(Set.of(newAddress), addresses.asSet());
    }

    @Test
    void addressUpdatedEventIsEmittedWhenAddressIsAddedToParty() {
        //given
        PartyId partyId = PartyId.random();
        Address address = thereIsSomeGeoAddressFor(partyId);

        //and
        Address newAddress = GeoAddressFixture.someGeoAddressWith(address.id(), partyId);

        //and
        GeoAddressUpdated expectedEvent = (GeoAddressUpdated) newAddress.toAddressUpdateSucceededEvent();

        //when
        addressesFacade.addOrUpdate(partyId, newAddress);

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    @Test
    void canRemoveExistingAddressOfParty() {
        //given
        PartyId partyId = PartyId.random();
        Address address = thereIsSomeGeoAddressFor(partyId);

        //when
        addressesFacade.remove(partyId, address.id());

        //then
        Addresses addresses = addressesFacade.findFor(partyId).get();
        assertTrue(addresses.asSet().isEmpty());
    }

    @Test
    void addressRemovedEventIsEmittedWhenAddressIsAddedToParty() {
        //given
        PartyId partyId = PartyId.random();
        Address address = thereIsSomeGeoAddressFor(partyId);

        //and
        GeoAddressRemoved expectedEvent = (GeoAddressRemoved) address.toAddressRemovalSucceededEvent();

        //when
        addressesFacade.remove(partyId, address.id());

        //then
        assertTrue(testEventListener.thereIsAnEventEqualTo(expectedEvent));
    }

    private Address thereIsSomeGeoAddressFor(PartyId partyId) {
        Address address = GeoAddressFixture.someGeoAddressFor(partyId);
        addressesFacade.addOrUpdate(partyId, address);
        return address;
    }

    static class AddressesTestEventListener implements InMemoryEventsPublisher.InMemoryEventObserver {

        private BlockingQueue<PublishedEvent> events = new LinkedBlockingQueue<>();

        AddressesTestEventListener(InMemoryEventsPublisher eventsPublisher) {
            eventsPublisher.register(this);
        }

        @Override
        public void handle(PublishedEvent event) {
            events.add(event);
        }

        Optional<PublishedEvent> findMatching(Predicate<PublishedEvent> predicate) {
            return events.stream().filter(predicate).findFirst();
        }

        boolean thereIsAnEventEqualTo(PublishedEvent expectedEvent) {
            return findMatching(it -> it.equals(expectedEvent)).isPresent();
        }
    }
}
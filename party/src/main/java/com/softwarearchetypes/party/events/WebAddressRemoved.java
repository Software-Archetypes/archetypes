package com.softwarearchetypes.party.events;

public record WebAddressRemoved(String addressId, String partyId) implements AddressRemovalSucceeded, PublishedEvent {

}

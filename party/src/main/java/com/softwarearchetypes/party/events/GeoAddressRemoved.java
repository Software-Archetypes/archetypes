package com.softwarearchetypes.party.events;

public record GeoAddressRemoved(String addressId, String partyId) implements AddressRemovalSucceeded, PublishedEvent {

}

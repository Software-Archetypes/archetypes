package com.softwarearchetypes.party.events;

public record PhoneAddressRemoved(String addressId, String partyId) implements AddressRemovalSucceeded, PublishedEvent {

}

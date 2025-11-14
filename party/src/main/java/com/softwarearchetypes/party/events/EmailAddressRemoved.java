package com.softwarearchetypes.party.events;

public record EmailAddressRemoved(String addressId, String partyId) implements AddressRemovalSucceeded, PublishedEvent {

}

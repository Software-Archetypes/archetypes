package com.softwarearchetypes.events;

public record GeoAddressRemoved(String addressId, String partyId) implements AddressRemovalSucceeded {

}

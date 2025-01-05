package com.softwarearchetypes.party.events;

public sealed interface AddressRemovalSucceeded extends AddressRelatedEvent permits AddressRemovalSkipped, GeoAddressRemoved {

}

package com.softwarearchetypes.events;

public sealed interface AddressRemovalSucceeded extends AddressRelatedEvent permits AddressRemovalSkipped, GeoAddressRemoved {

}

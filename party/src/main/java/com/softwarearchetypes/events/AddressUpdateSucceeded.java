package com.softwarearchetypes.events;

public sealed interface AddressUpdateSucceeded extends AddressRelatedEvent permits AddressUpdateSkipped, GeoAddressUpdated {

}

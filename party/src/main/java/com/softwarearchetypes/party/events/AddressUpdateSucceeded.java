package com.softwarearchetypes.party.events;

public sealed interface AddressUpdateSucceeded extends AddressRelatedEvent permits AddressUpdateSkipped, GeoAddressUpdated {

}

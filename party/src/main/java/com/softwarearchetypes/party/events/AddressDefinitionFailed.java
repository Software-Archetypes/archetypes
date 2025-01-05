package com.softwarearchetypes.party.events;

public sealed interface AddressDefinitionFailed extends AddressRelatedEvent permits AddressAdditionFailed, AddressUpdateFailed {

}

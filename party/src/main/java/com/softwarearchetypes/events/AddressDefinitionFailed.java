package com.softwarearchetypes.events;

public sealed interface AddressDefinitionFailed extends AddressRelatedEvent permits AddressAdditionFailed, AddressUpdateFailed {

}

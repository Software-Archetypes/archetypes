package com.softwarearchetypes.events;

public sealed interface RegisteredIdentifierRemovalSucceeded extends PartyRelatedEvent permits RegisteredIdentifierRemovalSkipped, RegisteredIdentifierRemoved {

}

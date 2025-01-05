package com.softwarearchetypes.party.events;

public sealed interface RegisteredIdentifierRemovalSucceeded extends PartyRelatedEvent permits RegisteredIdentifierRemovalSkipped, RegisteredIdentifierRemoved {

}

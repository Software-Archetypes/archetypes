package com.softwarearchetypes.party.events;

public record RegisteredIdentifierRemovalFailed(String partyId, String identifier, String reason) implements PartyRelatedFailureEvent {
}

package com.softwarearchetypes.party.events;

public record RegisteredIdentifierAdditionFailed(String partyId, String identifier, String reason) implements PartyRelatedFailureEvent {
}

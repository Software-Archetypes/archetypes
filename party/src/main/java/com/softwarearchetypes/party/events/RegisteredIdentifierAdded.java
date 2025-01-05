package com.softwarearchetypes.party.events;

public record RegisteredIdentifierAdded(String partyId, String type, String value) implements RegisteredIdentifierAdditionSucceeded, PublishedEvent {

}

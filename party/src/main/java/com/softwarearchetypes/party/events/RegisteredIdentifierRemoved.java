package com.softwarearchetypes.party.events;

public record RegisteredIdentifierRemoved(String partyId, String type, String value) implements RegisteredIdentifierRemovalSucceeded, PublishedEvent {

}

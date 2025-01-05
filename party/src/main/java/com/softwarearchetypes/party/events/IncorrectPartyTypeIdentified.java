package com.softwarearchetypes.party.events;

public record IncorrectPartyTypeIdentified(String partyId, String expectedType) implements PartyRelatedFailureEvent {

}

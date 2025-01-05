package com.softwarearchetypes.party.events;

public record PartyNotFound(String partyId) implements PartyRelatedFailureEvent {

}

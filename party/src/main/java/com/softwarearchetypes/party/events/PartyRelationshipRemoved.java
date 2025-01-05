package com.softwarearchetypes.party.events;

public record PartyRelationshipRemoved(String partyRelationshipId) implements PartyRelatedEvent, PublishedEvent {

}

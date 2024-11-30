package com.softwarearchetypes.party.events;

public record RoleRemoved(String partyId, String name) implements RoleRemovalSucceeded, PublishedEvent {

}

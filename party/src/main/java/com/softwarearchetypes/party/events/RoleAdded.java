package com.softwarearchetypes.party.events;

public record RoleAdded(String partyId, String name) implements RoleAdditionSucceeded, PublishedEvent {
}

package com.softwarearchetypes.party.events;

public record RoleRemovalFailed(String partyId, String role, String reason) implements PartyRelatedFailureEvent {
}

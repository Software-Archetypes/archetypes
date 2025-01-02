package com.softwarearchetypes.party.events;

public record RoleAdditionFailed(String partyId, String role, String reason) implements PartyRelatedFailureEvent {
}

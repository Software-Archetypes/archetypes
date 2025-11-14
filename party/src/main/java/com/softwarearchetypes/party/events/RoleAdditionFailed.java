package com.softwarearchetypes.party.events;

public record RoleAdditionFailed(String partyId, String role, String reason) implements PartyRelatedFailureEvent {

    public static RoleAdditionFailed dueTo(String partyId, String role, String reason) {
        return new RoleAdditionFailed(partyId, role, reason);
    }
}

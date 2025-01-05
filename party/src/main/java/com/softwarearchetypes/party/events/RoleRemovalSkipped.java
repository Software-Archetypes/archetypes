package com.softwarearchetypes.party.events;

public record RoleRemovalSkipped(String partyId, String name, String reason) implements RoleRemovalSucceeded {

    private static final String MISSING_ROLE_REASON = "MISSING_ROLE";

    public static RoleRemovalSkipped dueToMissingRoleFor(String partyId, String name) {
        return new RoleRemovalSkipped(partyId, name, MISSING_ROLE_REASON);
    }

}

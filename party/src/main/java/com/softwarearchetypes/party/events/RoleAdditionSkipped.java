package com.softwarearchetypes.party.events;

public record RoleAdditionSkipped(String partyId, String name, String reason) implements RoleAdditionSucceeded {

    private static final String DUPLICATION_REASON = "DUPLICATION";

    public static RoleAdditionSkipped dueToDuplicationFor(String partyId, String name) {
        return new RoleAdditionSkipped(partyId, name, DUPLICATION_REASON);
    }

}

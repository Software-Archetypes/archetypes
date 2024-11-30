package com.softwarearchetypes.party.events;

public record RegisteredIdentifierAdditionSkipped(String partyId, String type, String value, String reason) implements RegisteredIdentifierAdditionSucceeded {

    private static final String DUPLICATION_REASON = "DUPLICATION";

    public static RegisteredIdentifierAdditionSkipped dueToDataDuplicationFor(String partyId, String type, String value) {
        return new RegisteredIdentifierAdditionSkipped(partyId, type, value, DUPLICATION_REASON);
    }

}

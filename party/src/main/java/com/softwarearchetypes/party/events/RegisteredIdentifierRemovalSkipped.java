package com.softwarearchetypes.party.events;

public record RegisteredIdentifierRemovalSkipped(String partyId, String type, String value, String reason) implements RegisteredIdentifierRemovalSucceeded {

    private static final String MISSING_IDENTIFIER_REASON = "MISSING_IDENTIFIER";

    public static RegisteredIdentifierRemovalSkipped dueToMissingIdentifierFor(String partyId, String type, String value) {
        return new RegisteredIdentifierRemovalSkipped(partyId, type, value, MISSING_IDENTIFIER_REASON);
    }
}

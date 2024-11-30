package com.softwarearchetypes.party.events;

public record PersonalDataUpdateSkipped(String partyId, String firstName, String lastName, String reason) implements PersonalDataUpdateSucceeded {

    private static final String NO_CHANGE_IDENTIFIED_REASON = "NO_CHANGE_IDENTIFIED";

    public static PersonalDataUpdateSkipped dueToNoChangeIdentifiedFor(String partyId, String firstName, String lastName) {
        return new PersonalDataUpdateSkipped(partyId, firstName, lastName, NO_CHANGE_IDENTIFIED_REASON);
    }

}

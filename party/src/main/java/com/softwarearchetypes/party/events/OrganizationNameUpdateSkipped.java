package com.softwarearchetypes.party.events;

public record OrganizationNameUpdateSkipped(String partyId, String value, String reason) implements OrganizationNameUpdateSucceeded {

    private static final String NO_CHANGE_IDENTIFIED_REASON = "NO_CHANGE_IDENTIFIED";

    public static OrganizationNameUpdateSkipped dueToNoChangeIdentifiedFor(String partyId, String value) {
        return new OrganizationNameUpdateSkipped(partyId, value, NO_CHANGE_IDENTIFIED_REASON);
    }

}

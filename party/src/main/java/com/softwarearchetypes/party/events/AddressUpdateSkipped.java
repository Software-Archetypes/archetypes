package com.softwarearchetypes.party.events;

public record AddressUpdateSkipped(String addressId, String partyId, String reason) implements AddressUpdateSucceeded {

    private static final String NO_CHANGES_IDENTIFIED_REASON = "NO_CHANGES_IDENTIFIED";

    public static AddressUpdateSkipped dueToNoChangesIdentifiedFor(String addressId, String partyId) {
        return new AddressUpdateSkipped(addressId, partyId, NO_CHANGES_IDENTIFIED_REASON);
    }

}

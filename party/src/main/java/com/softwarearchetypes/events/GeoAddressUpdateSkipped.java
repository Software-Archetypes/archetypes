package com.softwarearchetypes.events;

public record GeoAddressUpdateSkipped(String addressId, String partyId, String reason) implements AddressUpdateSucceeded {

    private static final String NO_CHANGES_IDENTIFIED_REASON = "NO_CHANGES_IDENTIFIED";

    public static GeoAddressUpdateSkipped dueToNoChangesIdentifiedFor(String addressId, String partyId) {
        return new GeoAddressUpdateSkipped(addressId, partyId, NO_CHANGES_IDENTIFIED_REASON);
    }

}

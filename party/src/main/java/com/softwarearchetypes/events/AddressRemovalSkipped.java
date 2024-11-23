package com.softwarearchetypes.events;

public record AddressRemovalSkipped(String addressId, String partyId, String reason) implements AddressRemovalSucceeded {

    private static final String ADDRESS_NOT_FOUND_REASON = "ADDRESS_NOT_FOUND";

    public static AddressRemovalSkipped dueToAddressNotFoundFor(String addressId, String partyId) {
        return new AddressRemovalSkipped(addressId, partyId, ADDRESS_NOT_FOUND_REASON);
    }

}

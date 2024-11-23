package com.softwarearchetypes.events;

public record AddressAdditionFailed(String addressId, String partyId, String reason) implements AddressDefinitionFailed {

    private static final String POLICY_NOT_MET_REASON = "POLICY_NOT_MET";

    public static AddressAdditionFailed dueToPolicyNotMetFor(String addressId, String partyId) {
        return new AddressAdditionFailed(addressId, partyId, POLICY_NOT_MET_REASON);
    }
}

package com.softwarearchetypes.events;

public record AddressUpdateFailed(String reason) implements AddressDefinitionFailed {

    private static final String NOT_MATCHING_ADDRESS_TYPE_REASON = "NOT_MATCHING_ADDRESS_TYPE";

    public static AddressUpdateFailed dueToNotMatchingAddressType() {
        return new AddressUpdateFailed(NOT_MATCHING_ADDRESS_TYPE_REASON);
    }
}

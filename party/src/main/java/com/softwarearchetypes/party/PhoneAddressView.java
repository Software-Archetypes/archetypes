package com.softwarearchetypes.party;

import java.util.Set;

public record PhoneAddressView(
        AddressId addressId,
        PartyId partyId,
        String phoneNumber,
        Set<String> useTypes,
        Validity validity) implements AddressView {
}

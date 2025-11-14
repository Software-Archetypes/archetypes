package com.softwarearchetypes.party;

import java.util.Set;

public record WebAddressView(
        AddressId addressId,
        PartyId partyId,
        String url,
        Set<String> useTypes,
        Validity validity) implements AddressView {
}

package com.softwarearchetypes.party;

import java.util.Set;

public record EmailAddressView(
        AddressId addressId,
        PartyId partyId,
        String email,
        Set<String> useTypes,
        Validity validity) implements AddressView {
}

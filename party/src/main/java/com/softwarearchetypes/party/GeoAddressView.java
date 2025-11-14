package com.softwarearchetypes.party;

import java.util.Locale;
import java.util.Set;

public record GeoAddressView(
        AddressId addressId,
        PartyId partyId,
        String name,
        String street,
        String building,
        String flat,
        String city,
        String zipCode,
        Locale locale,
        Set<String> useTypes,
        Validity validity) implements AddressView {
}

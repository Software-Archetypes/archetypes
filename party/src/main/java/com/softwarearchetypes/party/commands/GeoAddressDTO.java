package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.AddressId;
import com.softwarearchetypes.party.PartyId;
import java.util.Locale;
import java.util.Set;

public record GeoAddressDTO(
        AddressId addressId,
        PartyId partyId,
        String name,
        String street,
        String building,
        String flat,
        String city,
        String zipCode,
        Locale locale,
        Set<String> useTypes) {
}

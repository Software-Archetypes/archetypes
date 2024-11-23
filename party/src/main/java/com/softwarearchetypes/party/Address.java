package com.softwarearchetypes.party;

import java.util.Set;

public sealed interface Address extends AddressLifecycle permits GeoAddress {

    AddressId id();

    PartyId partyId();

    Set<AddressUseType> useTypes();

    AddressDetails addressDetails();
}
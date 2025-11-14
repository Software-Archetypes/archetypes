package com.softwarearchetypes.party;

import java.util.Set;

public sealed interface AddressView permits GeoAddressView, EmailAddressView, PhoneAddressView, WebAddressView {

    AddressId addressId();

    PartyId partyId();

    Set<String> useTypes();

    Validity validity();
}

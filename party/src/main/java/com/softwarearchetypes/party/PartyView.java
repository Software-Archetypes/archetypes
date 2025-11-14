package com.softwarearchetypes.party;

import java.util.Set;

public sealed interface PartyView permits PersonView, CompanyView, OrganizationUnitView {

    PartyId partyId();

    String partyType();

    Set<String> roles();

    Set<RegisteredIdentifier> registeredIdentifiers();

    long version();
}

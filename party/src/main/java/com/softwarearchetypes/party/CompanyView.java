package com.softwarearchetypes.party;

import java.util.Set;

public record CompanyView(
        PartyId partyId,
        String organizationName,
        Set<String> roles,
        Set<RegisteredIdentifier> registeredIdentifiers,
        long version) implements PartyView {

    @Override
    public String partyType() {
        return "COMPANY";
    }
}

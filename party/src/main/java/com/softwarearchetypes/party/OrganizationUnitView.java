package com.softwarearchetypes.party;

import java.util.Set;

public record OrganizationUnitView(
        PartyId partyId,
        String organizationName,
        Set<String> roles,
        Set<RegisteredIdentifier> registeredIdentifiers,
        long version) implements PartyView {

    @Override
    public String partyType() {
        return "ORGANIZATION_UNIT";
    }
}

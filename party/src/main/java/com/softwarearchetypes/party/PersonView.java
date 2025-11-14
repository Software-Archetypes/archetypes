package com.softwarearchetypes.party;

import java.util.Set;

public record PersonView(
        PartyId partyId,
        String firstName,
        String lastName,
        Set<String> roles,
        Set<RegisteredIdentifier> registeredIdentifiers,
        long version) implements PartyView {

    @Override
    public String partyType() {
        return "PERSON";
    }
}

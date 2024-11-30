package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.Set;

import com.softwarearchetypes.common.Version;

sealed abstract class PartyAbstractTestDataBuilder<T extends Party> permits CompanyTestDataBuilder, OrganizationUnitTestDataBuilder, PersonTestDataBuilder {

    PartyId partyId;
    Set<Role> roles = new HashSet<>();
    Set<RegisteredIdentifier> registeredIdentifiers = new HashSet<>();
    Version version = Version.initial();

    PartyAbstractTestDataBuilder<T> withRandomPartyId() {
        partyId = PartyId.random();
        return this;
    }

    PartyAbstractTestDataBuilder<T> with(Role role) {
        roles.add(role);
        return this;
    }

    PartyAbstractTestDataBuilder<T> with(RegisteredIdentifier identifier) {
        registeredIdentifiers.add(identifier);
        return this;
    }

    abstract T build();
}

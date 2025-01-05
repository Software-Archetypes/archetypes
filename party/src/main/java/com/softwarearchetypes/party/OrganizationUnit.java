package com.softwarearchetypes.party;

import java.util.Set;

import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.party.events.OrganizationUnitRegistered;
import com.softwarearchetypes.party.events.PartyRegistered;

import static java.util.stream.Collectors.toSet;

public final class OrganizationUnit extends Organization {

    OrganizationUnit(PartyId partyId, OrganizationName organizationName, Set<Role> roles,
            Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
        super(partyId, organizationName, roles, registeredIdentifiers, version);
    }

    @Override
    PartyRegistered toPartyRegisteredEvent() {
        return new OrganizationUnitRegistered(id().asString(), organizationName().value(),
                registeredIdentifiers().stream().map(RegisteredIdentifier::asString).collect(toSet()),
                roles().stream().map(Role::asString).collect(toSet()));
    }
}

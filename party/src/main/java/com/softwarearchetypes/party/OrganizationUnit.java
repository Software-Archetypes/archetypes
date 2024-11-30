package com.softwarearchetypes.party;

import java.util.Set;

import com.softwarearchetypes.common.Version;

public final class OrganizationUnit extends Organization {

    OrganizationUnit(PartyId partyId, OrganizationName organizationName, Set<Role> roles,
            Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
        super(partyId, organizationName, roles, registeredIdentifiers, version);
    }

}

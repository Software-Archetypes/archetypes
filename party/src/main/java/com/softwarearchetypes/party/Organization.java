package com.softwarearchetypes.party;

import java.util.Set;

import com.softwarearchetypes.common.Version;

public sealed abstract class Organization extends Party permits Company, OrganizationUnit {

    private final OrganizationName organizationName;

    Organization(PartyId partyId, OrganizationName organizationName, Set<Role> roles,
            Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
        super(partyId, roles, registeredIdentifiers, version);
        this.organizationName = organizationName;
    }

    public final OrganizationName organizationName() {
        return this.organizationName;
    }
}

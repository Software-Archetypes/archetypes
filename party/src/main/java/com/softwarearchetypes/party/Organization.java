package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Version;

public sealed abstract class Organization extends Party permits Company, OrganizationUnit {

    private final OrganizationName organizationName;

    Organization(PartyId partyId, OrganizationName organizationName, Addresses addresses, Roles roles,
            RegisteredIdentifiers registeredIdentifiers, Version version) {
        super(partyId, addresses, roles, registeredIdentifiers, version);
        this.organizationName = organizationName;
    }

    public final OrganizationName organizationName() {
        return this.organizationName;
    }
}

package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Version;

public final class OrganizationUnit extends Organization {

    OrganizationUnit(PartyId partyId, OrganizationName organizationName, Addresses addresses, Roles roles,
            RegisteredIdentifiers registeredIdentifiers, Version version) {
        super(partyId, organizationName, addresses, roles, registeredIdentifiers, version);
    }

}

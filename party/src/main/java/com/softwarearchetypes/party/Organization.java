package com.softwarearchetypes.party;

import java.util.Set;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.party.events.OrganizationNameUpdateFailed;
import com.softwarearchetypes.party.events.OrganizationNameUpdateSkipped;
import com.softwarearchetypes.party.events.OrganizationNameUpdated;

public sealed abstract class Organization extends Party permits Company, OrganizationUnit {

    private OrganizationName organizationName;

    Organization(PartyId partyId, OrganizationName organizationName, Set<Role> roles,
            Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
        super(partyId, roles, registeredIdentifiers, version);
        this.organizationName = organizationName;
    }

    public Result<OrganizationNameUpdateFailed, Organization> update(OrganizationName organizationName) {
        if (!this.organizationName.equals(organizationName)) {
            this.organizationName = organizationName;
            register(new OrganizationNameUpdated(id().asString(), organizationName.value()));
        } else {
            register(OrganizationNameUpdateSkipped.dueToNoChangeIdentifiedFor(id().asString(), organizationName.value()));
        }
        return Result.success(this);
    }

    public final OrganizationName organizationName() {
        return this.organizationName;
    }
}

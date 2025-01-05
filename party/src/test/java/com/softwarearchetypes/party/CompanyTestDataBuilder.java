package com.softwarearchetypes.party;

final class CompanyTestDataBuilder extends PartyAbstractTestDataBuilder<Company> {

    OrganizationName organizationName;

    CompanyTestDataBuilder with(OrganizationName organizationName) {
        this.organizationName = organizationName;
        return this;
    }

    @Override
    Company build() {
        return new Company(partyId, organizationName, roles, registeredIdentifiers, version);
    }
}

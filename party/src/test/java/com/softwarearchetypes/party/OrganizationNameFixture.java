package com.softwarearchetypes.party;

import com.softwarearchetypes.common.RandomFixture;

class OrganizationNameFixture {

    static OrganizationName someOrganizationName() {
        return OrganizationName.of(RandomFixture.randomStringWithPrefixOf("organizationName"));
    }
}

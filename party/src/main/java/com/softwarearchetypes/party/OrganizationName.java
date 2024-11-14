package com.softwarearchetypes.party;

public record OrganizationName(String value) {

    static OrganizationName of(String value) {
        return new OrganizationName(value);
    }

}

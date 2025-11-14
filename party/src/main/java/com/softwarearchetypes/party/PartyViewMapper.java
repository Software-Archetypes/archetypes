package com.softwarearchetypes.party;

import java.util.stream.Collectors;

public class PartyViewMapper {

    public static PartyView toView(Party party) {
        return switch (party) {
            case Person person -> new PersonView(
                    person.id(),
                    person.personalData().firstName(),
                    person.personalData().lastName(),
                    party.roles().stream().map(Role::asString).collect(Collectors.toSet()),
                    party.registeredIdentifiers(),
                    party.version().value()
            );
            case Company company -> new CompanyView(
                    company.id(),
                    company.organizationName().asString(),
                    party.roles().stream().map(Role::asString).collect(Collectors.toSet()),
                    party.registeredIdentifiers(),
                    party.version().value()
            );
            case OrganizationUnit organizationUnit -> new OrganizationUnitView(
                    organizationUnit.id(),
                    organizationUnit.organizationName().asString(),
                    party.roles().stream().map(Role::asString).collect(Collectors.toSet()),
                    party.registeredIdentifiers(),
                    party.version().value()
            );
        };
    }
}

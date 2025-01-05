package com.softwarearchetypes.party;

import static com.softwarearchetypes.party.OrganizationNameFixture.someOrganizationName;
import static com.softwarearchetypes.party.PersonalDataFixture.somePersonalData;
import static com.softwarearchetypes.party.RegisteredIdentifierFixture.someIdentifierSetOfSize;
import static com.softwarearchetypes.party.RoleFixture.someRoleSetOfSize;

class PartyTestSupport {

    private final PartyRepository partyRepository;

    PartyTestSupport(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    Person thereIsSomePerson() {
        Person party = PartyFixture.somePerson()
                                   .with(somePersonalData())
                                   .withRandomPartyId()
                                   .withRoleSetOf(someRoleSetOfSize(5))
                                   .withRegisteredIdentifierSetOf(someIdentifierSetOfSize(5))
                                   .build();
        return (Person) thereIs(party);
    }

    Organization thereIsSomeOrganization() {
        Company party = PartyFixture.someCompany()
                                    .with(someOrganizationName())
                                    .withRandomPartyId()
                                    .withRoleSetOf(someRoleSetOfSize(5))
                                    .withRegisteredIdentifierSetOf(someIdentifierSetOfSize(5))
                                    .build();
        return (Organization) thereIs(party);
    }

    Party thereIs(Party party) {
        partyRepository.save(party);
        return party;
    }
}

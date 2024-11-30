package com.softwarearchetypes.party;

final class PersonTestDataBuilder extends PartyAbstractTestDataBuilder<Person> {

    PersonalData personalData = PersonalData.empty();

    PersonTestDataBuilder with(PersonalData personalData) {
        this.personalData = personalData;
        return this;
    }

    @Override
    Person build() {
        return new Person(partyId, personalData, roles, registeredIdentifiers, version);
    }
}

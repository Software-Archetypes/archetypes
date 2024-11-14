package com.softwarearchetypes.party;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.events.PersonalDataUpdateFailed;
import com.softwarearchetypes.events.PersonalDataUpdated;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

public final class Person extends Party {

    private PersonalData personalData;

    Person(PartyId id, PersonalData personalData, Addresses addresses, Roles roles, RegisteredIdentifiers registeredIdentifiers, Version version) {
        super(id, addresses, roles, registeredIdentifiers, version);
        checkArgument(personalData != null, "Personal data cannot be null");
        this.personalData = personalData;
    }

    public Result<PersonalDataUpdateFailed, Person> update(PersonalData personalData) {
        this.personalData = personalData;
        register(new PersonalDataUpdated(personalData.firstName(), personalData.lastName()));
        return Result.success(this);
    }

    public PersonalData personalData() {
        return this.personalData;
    }
}

package com.softwarearchetypes.party;

import java.util.Set;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.common.Version;
import com.softwarearchetypes.party.events.PersonalDataUpdateFailed;
import com.softwarearchetypes.party.events.PersonalDataUpdateSkipped;
import com.softwarearchetypes.party.events.PersonalDataUpdated;

import static com.softwarearchetypes.common.Preconditions.checkArgument;

public final class Person extends Party {

    private PersonalData personalData;

    Person(PartyId id, PersonalData personalData, Set<Role> roles, Set<RegisteredIdentifier> registeredIdentifiers, Version version) {
        super(id, roles, registeredIdentifiers, version);
        checkArgument(personalData != null, "Personal data cannot be null");
        this.personalData = personalData;
    }

    public Result<PersonalDataUpdateFailed, Person> update(PersonalData personalData) {
        if (!this.personalData.equals(personalData)) {
            this.personalData = personalData;
            register(new PersonalDataUpdated(id().asString(), personalData.firstName(), personalData.lastName()));
            return Result.success(this);
        } else {
            register(PersonalDataUpdateSkipped.dueToNoChangeIdentifiedFor(id().asString(), personalData.firstName(), personalData.lastName()));
            return Result.success(this);
        }
    }

    public PersonalData personalData() {
        return this.personalData;
    }
}

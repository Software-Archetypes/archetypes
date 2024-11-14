package com.softwarearchetypes.party;

import java.util.Objects;
import java.util.Optional;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.PersonalDataUpdateFailed;
import com.softwarearchetypes.events.PersonalDataUpdateSkipped;
import com.softwarearchetypes.events.PersonalDataUpdateSucceeded;
import com.softwarearchetypes.events.PersonalDataUpdated;

public final class PersonalData {

    private static final String EMPTY = "";
    private String firstName;
    private String lastName;

    private PersonalData(String firstName, String lastName) {
        this.firstName = Optional.ofNullable(firstName).orElse(EMPTY);
        this.lastName = Optional.ofNullable(lastName).orElse(EMPTY);
    }

    public static PersonalData from(String firstName, String lastName) {
        return new PersonalData(firstName, lastName);
    }

    Result<PersonalDataUpdateFailed, PersonalDataUpdateSucceeded> updateWith(PersonalData personalData) {
        if (!this.equals(personalData)) {
            this.firstName = personalData.firstName();
            this.lastName = personalData.lastName();
            return Result.success(new PersonalDataUpdated(personalData.firstName(), personalData.lastName()));
        } else {
            return Result.success(PersonalDataUpdateSkipped.dueToNoChangeIdentifiedFor(personalData.firstName(), personalData.lastName()));
        }
    }

    String firstName() {
        return firstName;
    }

    String lastName() {
        return lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PersonalData that)) {
            return false;
        }
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName);
    }
}

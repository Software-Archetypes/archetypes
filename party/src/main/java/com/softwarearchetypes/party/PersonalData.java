package com.softwarearchetypes.party;

import java.util.Optional;

public record PersonalData(String firstName, String lastName) {

    private static final String EMPTY = "";

    public PersonalData {
        firstName = Optional.ofNullable(firstName).orElse(EMPTY);
        lastName = Optional.ofNullable(lastName).orElse(EMPTY);
    }

    public static PersonalData from(String firstName, String lastName) {
        return new PersonalData(firstName, lastName);
    }

    static PersonalData empty() {
        return new PersonalData(EMPTY, EMPTY);
    }
}

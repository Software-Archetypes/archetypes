package com.softwarearchetypes.party;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

class PersonalDataFixture {

    static String someFirstName() {
        return randomAlphabetic(10);
    }

    static String someLastName() {
        return randomAlphabetic(10);
    }

    static PersonalData somePersonalData() {
        return PersonalData.from(someFirstName(), someLastName());
    }

    static PersonalData nameOf(String firstName) {
        return PersonalData.from(firstName, null);
    }
}

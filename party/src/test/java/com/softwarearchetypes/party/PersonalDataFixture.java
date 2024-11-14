package com.softwarearchetypes.party;

import org.apache.commons.lang3.RandomStringUtils;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

class PersonalDataFixture {

    static String someFirstName() {
        return randomAlphabetic(10);
    }

    static String someLastName() {
        return randomAlphabetic(10);
    }
}

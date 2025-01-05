package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomNumeric;

class PersonalIdentificationNumberTest {

    private static final int PERSONAL_ID_VALUE_LENGTH = 11;

    @Test
    void twoPersonalIdentificationNumbersShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        PersonalIdentificationNumber firstNumber = PersonalIdentificationNumber.of(randomNumeric(PERSONAL_ID_VALUE_LENGTH));
        PersonalIdentificationNumber secondNumber = PersonalIdentificationNumber.of(randomNumeric(PERSONAL_ID_VALUE_LENGTH));

        //expect
        assertNotEquals(firstNumber, secondNumber);
    }

    @Test
    void twoPersonalIdentificationNumbersShouldBeEqualWhenCreatedForTheSameValues() {
        //given
        String value = randomNumeric(PERSONAL_ID_VALUE_LENGTH);

        //expect
        assertEquals(PersonalIdentificationNumber.of(value), PersonalIdentificationNumber.of(value));
    }

    @Test
    void personalIdentificationNumberShouldBeConvertibleToTheValueItWasCreatedFrom() {
        //given
        String value = randomNumeric(PERSONAL_ID_VALUE_LENGTH);
        PersonalIdentificationNumber number = PersonalIdentificationNumber.of(value);

        //expect
        assertEquals(value, number.asString());
    }

    @Test
    void shouldNotAllowToCreateAddressIdForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(null));
    }

    @Test
    void shouldNotAllowToCreateAddressIdForValueContainingLetters() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(randomAlphabetic(PERSONAL_ID_VALUE_LENGTH)));
    }

    @Test
    void shouldNotAllowToCreateAddressIdForValueShorterThanRequired() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(randomAlphabetic(PERSONAL_ID_VALUE_LENGTH - 1)));
    }

    @Test
    void shouldNotAllowToCreateAddressIdForValueLongerThanRequired() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(randomAlphabetic(PERSONAL_ID_VALUE_LENGTH + 1)));
    }
}
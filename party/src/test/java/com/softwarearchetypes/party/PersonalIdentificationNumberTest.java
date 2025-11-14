package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomNumeric;

class PersonalIdentificationNumberTest {

    private static final int PERSONAL_ID_VALUE_LENGTH = 11;
    private static final int[] CHECKSUM_WEIGHTS = {1, 3, 7, 9, 1, 3, 7, 9, 1, 3};

    @Test
    void twoPersonalIdentificationNumbersShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        PersonalIdentificationNumber firstNumber = PersonalIdentificationNumber.of(generateValidPESEL());
        PersonalIdentificationNumber secondNumber = PersonalIdentificationNumber.of(generateValidPESEL());

        //expect
        assertNotEquals(firstNumber, secondNumber);
    }

    @Test
    void twoPersonalIdentificationNumbersShouldBeEqualWhenCreatedForTheSameValues() {
        //given
        String value = generateValidPESEL();

        //expect
        assertEquals(PersonalIdentificationNumber.of(value), PersonalIdentificationNumber.of(value));
    }

    @Test
    void personalIdentificationNumberShouldBeConvertibleToTheValueItWasCreatedFrom() {
        //given
        String value = generateValidPESEL();
        PersonalIdentificationNumber number = PersonalIdentificationNumber.of(value);

        //expect
        assertEquals(value, number.asString());
    }

    @Test
    void personalIdentificationNumberShouldReturnCorrectType() {
        //given
        PersonalIdentificationNumber number = PersonalIdentificationNumber.of(generateValidPESEL());

        //expect
        assertEquals("PERSONAL_IDENTIFICATION_NUMBER", number.type());
    }

    @Test
    void shouldAcceptValidPESELWithCorrectChecksum() {
        //given - valid PESEL: 44051401458 (born 1944-05-14)
        String validPESEL = "44051401458";

        //expect
        PersonalIdentificationNumber number = PersonalIdentificationNumber.of(validPESEL);
        assertEquals(validPESEL, number.asString());
    }

    @Test
    void shouldNotAllowToCreatePersonalIdentificationNumberForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(null));
    }

    @Test
    void shouldNotAllowToCreatePersonalIdentificationNumberForValueContainingLetters() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(randomAlphabetic(PERSONAL_ID_VALUE_LENGTH)));
    }

    @Test
    void shouldNotAllowToCreatePersonalIdentificationNumberForValueShorterThanRequired() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(randomNumeric(PERSONAL_ID_VALUE_LENGTH - 1)));
    }

    @Test
    void shouldNotAllowToCreatePersonalIdentificationNumberForValueLongerThanRequired() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(randomNumeric(PERSONAL_ID_VALUE_LENGTH + 1)));
    }

    @Test
    void shouldNotAllowToCreatePersonalIdentificationNumberWithInvalidChecksum() {
        //given - invalid checksum (last digit should be 8, not 9)
        String invalidPESEL = "44051401459";

        //expect
        assertThrows(IllegalArgumentException.class, () -> PersonalIdentificationNumber.of(invalidPESEL));
    }

    /**
     * Generates a valid PESEL with correct checksum for testing purposes.
     */
    private String generateValidPESEL() {
        String withoutChecksum = randomNumeric(10);
        int checksum = calculateChecksum(withoutChecksum);
        return withoutChecksum + checksum;
    }

    private int calculateChecksum(String first10Digits) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            int digit = Character.getNumericValue(first10Digits.charAt(i));
            sum += digit * CHECKSUM_WEIGHTS[i];
        }
        return (10 - (sum % 10)) % 10;
    }
}
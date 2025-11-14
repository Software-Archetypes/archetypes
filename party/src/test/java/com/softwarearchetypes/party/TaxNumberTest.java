package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomNumeric;

class TaxNumberTest {

    private static final int TAX_NUMBER_VALUE_LENGTH = 10;
    private static final int[] CHECKSUM_WEIGHTS = {6, 5, 7, 2, 3, 4, 5, 6, 7};

    @Test
    void twoTaxNumbersShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        TaxNumber firstNumber = TaxNumber.of(generateValidNIP());
        TaxNumber secondNumber = TaxNumber.of(generateValidNIP());

        //expect
        assertNotEquals(firstNumber, secondNumber);
    }

    @Test
    void twoTaxNumbersShouldBeEqualWhenCreatedForTheSameValues() {
        //given
        String value = generateValidNIP();

        //expect
        assertEquals(TaxNumber.of(value), TaxNumber.of(value));
    }

    @Test
    void taxNumberShouldBeConvertibleToTheValueItWasCreatedFrom() {
        //given
        String value = generateValidNIP();
        TaxNumber taxNumber = TaxNumber.of(value);

        //expect
        assertEquals(value, taxNumber.asString());
    }

    @Test
    void taxNumberShouldReturnCorrectType() {
        //given
        TaxNumber taxNumber = TaxNumber.of(generateValidNIP());

        //expect
        assertEquals("TAX_NUMBER", taxNumber.type());
    }

    @Test
    void shouldAcceptValidNIPWithCorrectChecksum() {
        //given - valid NIP: 1234563218
        String validNIP = "1234563218";

        //expect
        TaxNumber taxNumber = TaxNumber.of(validNIP);
        assertEquals(validNIP, taxNumber.asString());
    }

    @Test
    void shouldNotAllowToCreateTaxNumberForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> TaxNumber.of(null));
    }

    @Test
    void shouldNotAllowToCreateTaxNumberForValueContainingLetters() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> TaxNumber.of(randomAlphabetic(TAX_NUMBER_VALUE_LENGTH)));
    }

    @Test
    void shouldNotAllowToCreateTaxNumberForValueShorterThanRequired() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> TaxNumber.of(randomNumeric(TAX_NUMBER_VALUE_LENGTH - 1)));
    }

    @Test
    void shouldNotAllowToCreateTaxNumberForValueLongerThanRequired() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> TaxNumber.of(randomNumeric(TAX_NUMBER_VALUE_LENGTH + 1)));
    }

    @Test
    void shouldNotAllowToCreateTaxNumberWithInvalidChecksum() {
        //given - invalid checksum (last digit should be 8, not 9)
        String invalidNIP = "1234563219";

        //expect
        assertThrows(IllegalArgumentException.class, () -> TaxNumber.of(invalidNIP));
    }

    @Test
    void shouldNotAllowToCreateTaxNumberWhenChecksumEquals10() {
        //given - NIP where modulo 11 would equal 10 (invalid case)
        // "003000000X" where X can be any digit
        // sum = 0*6 + 0*5 + 3*7 + 0*2 + 0*3 + 0*4 + 0*5 + 0*6 + 0*7 = 21
        // 21 % 11 = 10 (invalid NIP - no valid checksum digit exists)
        String invalidNIP = "0030000000"; // Trying with 0 as last digit, but checksum should be 10 which is invalid

        //expect
        assertThrows(IllegalArgumentException.class, () -> TaxNumber.of(invalidNIP));
    }

    /**
     * Generates a valid NIP with correct checksum for testing purposes.
     */
    private String generateValidNIP() {
        String withoutChecksum;
        int checksum;

        do {
            withoutChecksum = randomNumeric(9);
            checksum = calculateChecksum(withoutChecksum);
        } while (checksum == 10); // Keep generating until we get valid checksum (not 10)

        return withoutChecksum + checksum;
    }

    private int calculateChecksum(String first9Digits) {
        int sum = 0;
        for (int i = 0; i < 9; i++) {
            int digit = Character.getNumericValue(first9Digits.charAt(i));
            sum += digit * CHECKSUM_WEIGHTS[i];
        }
        return sum % 11;
    }
}

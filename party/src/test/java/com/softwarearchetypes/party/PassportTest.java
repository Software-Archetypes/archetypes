package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class PassportTest {

    @Test
    void shouldCreatePassportWithValidityPeriod() {
        //given
        String passportNumber = "AB1234567";
        Validity validity = Validity.between(
                Instant.now().minus(365, ChronoUnit.DAYS),
                Instant.now().plus(365, ChronoUnit.DAYS)
        );

        //when
        Passport passport = Passport.of(passportNumber, validity);

        //then
        assertEquals(passportNumber, passport.asString());
        assertEquals("PASSPORT", passport.type());
        assertEquals(validity, passport.validity());
    }

    @Test
    void shouldCheckIfPassportIsCurrentlyValid() {
        //given - passport valid from yesterday to tomorrow
        Passport passport = Passport.of("XY9876543", Validity.between(
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS)
        ));

        //expect
        assertTrue(passport.isCurrentlyValid());
    }

    @Test
    void shouldCheckIfPassportHasExpired() {
        //given - passport expired yesterday
        Passport passport = Passport.of("CD5555555", Validity.until(
                Instant.now().minus(1, ChronoUnit.DAYS)
        ));

        //expect
        assertFalse(passport.isCurrentlyValid());
        assertTrue(passport.validity().hasExpired(Instant.now()));
    }

    @Test
    void shouldCheckIfPassportIsValidAtSpecificInstant() {
        //given
        Instant validFrom = Instant.parse("2020-01-01T00:00:00Z");
        Instant validTo = Instant.parse("2030-01-01T00:00:00Z");
        Passport passport = Passport.of("EF7777777", Validity.between(validFrom, validTo));

        //expect - valid in the middle of period
        assertTrue(passport.isValidAt(Instant.parse("2025-06-15T12:00:00Z")));

        //expect - not valid before period
        assertFalse(passport.isValidAt(Instant.parse("2019-12-31T23:59:59Z")));

        //expect - not valid after period (validTo is exclusive)
        assertFalse(passport.isValidAt(Instant.parse("2030-01-01T00:00:00Z")));
    }

    @Test
    void shouldNotAllowToCreatePassportWithNullValidity() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> Passport.of("GH1111111", null));
    }

    @Test
    void shouldNotAllowToCreatePassportWithInvalidFormat() {
        //given - invalid format (should be 2 letters + 7 digits)
        String invalidPassportNumber = "123456789"; // Only digits

        //expect
        assertThrows(IllegalArgumentException.class, () ->
            Passport.of(invalidPassportNumber, Validity.always())
        );
    }

    @Test
    void shouldNotAllowToCreatePassportWithTooShortNumber() {
        //given
        String shortPassportNumber = "AB123"; // Too short

        //expect
        assertThrows(IllegalArgumentException.class, () ->
            Passport.of(shortPassportNumber, Validity.always())
        );
    }

    @Test
    void twoPassportsShouldBeEqualWhenHaveSameNumberAndValidity() {
        //given
        Validity validity = Validity.between(
                Instant.parse("2020-01-01T00:00:00Z"),
                Instant.parse("2030-01-01T00:00:00Z")
        );
        Passport passport1 = Passport.of("IJ9999999", validity);
        Passport passport2 = Passport.of("IJ9999999", validity);

        //expect
        assertEquals(passport1, passport2);
    }
}

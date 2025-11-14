package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class ValidityTest {

    @Test
    void shouldCreateAlwaysValidPeriod() {
        //when
        Validity validity = Validity.always();

        //then
        assertTrue(validity.isValidAt(Instant.EPOCH));
        assertTrue(validity.isValidAt(Instant.now()));
        assertTrue(validity.isValidAt(Instant.MAX.minus(1, ChronoUnit.DAYS)));
        assertFalse(validity.hasExpired(Instant.now()));
    }

    @Test
    void shouldCreateValidityFromSpecificInstant() {
        //given
        Instant from = Instant.parse("2025-01-01T00:00:00Z");

        //when
        Validity validity = Validity.from(from);

        //then
        assertFalse(validity.isValidAt(Instant.parse("2024-12-31T23:59:59Z")));
        assertTrue(validity.isValidAt(Instant.parse("2025-01-01T00:00:00Z")));
        assertTrue(validity.isValidAt(Instant.parse("2030-01-01T00:00:00Z")));
    }

    @Test
    void shouldCreateValidityUntilSpecificInstant() {
        //given
        Instant until = Instant.parse("2030-12-31T23:59:59Z");

        //when
        Validity validity = Validity.until(until);

        //then
        assertTrue(validity.isValidAt(Instant.EPOCH));
        assertTrue(validity.isValidAt(Instant.parse("2025-06-15T12:00:00Z")));
        assertFalse(validity.isValidAt(Instant.parse("2030-12-31T23:59:59Z"))); // validTo is exclusive
    }

    @Test
    void shouldCreateValidityBetweenTwoInstants() {
        //given
        Instant from = Instant.parse("2020-01-01T00:00:00Z");
        Instant to = Instant.parse("2030-01-01T00:00:00Z");

        //when
        Validity validity = Validity.between(from, to);

        //then
        assertFalse(validity.isValidAt(Instant.parse("2019-12-31T23:59:59Z")));
        assertTrue(validity.isValidAt(Instant.parse("2020-01-01T00:00:00Z"))); // validFrom is inclusive
        assertTrue(validity.isValidAt(Instant.parse("2025-06-15T12:00:00Z")));
        assertFalse(validity.isValidAt(Instant.parse("2030-01-01T00:00:00Z"))); // validTo is exclusive
    }

    @Test
    void shouldCheckIfValidityHasExpired() {
        //given
        Validity expired = Validity.until(Instant.now().minus(1, ChronoUnit.DAYS));
        Validity notExpired = Validity.until(Instant.now().plus(1, ChronoUnit.DAYS));

        //expect
        assertTrue(expired.hasExpired(Instant.now()));
        assertFalse(notExpired.hasExpired(Instant.now()));
    }

    @Test
    void shouldHandleNullInstantsInBetweenMethod() {
        //when
        Validity always = Validity.between(null, null);
        Validity from = Validity.between(Instant.parse("2020-01-01T00:00:00Z"), null);
        Validity until = Validity.between(null, Instant.parse("2030-01-01T00:00:00Z"));

        //then
        assertEquals(Validity.ALWAYS, always);
        assertTrue(from.isValidAt(Instant.parse("2025-06-15T12:00:00Z")));
        assertTrue(until.isValidAt(Instant.parse("2025-06-15T12:00:00Z")));
    }

    @Test
    void shouldCheckCurrentValidity() {
        //given
        Validity currentlyValid = Validity.between(
                Instant.now().minus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS)
        );
        Validity expired = Validity.until(Instant.now().minus(1, ChronoUnit.DAYS));

        //expect
        assertTrue(currentlyValid.isCurrentlyValid());
        assertFalse(expired.isCurrentlyValid());
    }
}

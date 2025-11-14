package com.softwarearchetypes.accounting;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidityTest {

    static final Instant PAST = LocalDateTime.of(2024, 1, 10, 12, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant NOW = LocalDateTime.of(2024, 1, 15, 12, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant FUTURE = LocalDateTime.of(2024, 1, 20, 12, 0).atZone(ZoneId.systemDefault()).toInstant();
    static final Instant FOREVER = Instant.MAX.minusNanos(1);

    @Test
    void can_create_validity_until_specific_date() {
        //when
        Validity validity = Validity.until(NOW);

        //then
        assertEquals(Instant.EPOCH, validity.validFrom());
        assertEquals(NOW, validity.validTo());
    }

    @Test
    void can_create_validity_from_specific_date() {
        //when
        Validity validity = Validity.from(NOW);

        //then
        assertEquals(NOW, validity.validFrom());
        assertEquals(Instant.MAX, validity.validTo());
    }

    @Test
    void can_create_validity_between_dates() {
        //when
        Validity validity = Validity.between(PAST, FUTURE);

        //then
        assertEquals(PAST, validity.validFrom());
        assertEquals(FUTURE, validity.validTo());
    }

    @Test
    void can_create_always_valid_validity() {
        //when
        Validity validity = Validity.always();

        //then
        assertEquals(Instant.EPOCH, validity.validFrom());
        assertEquals(Instant.MAX, validity.validTo());
    }

    @Test
    void is_valid_at_instant_within_range() {
        //given
        Validity validity = Validity.between(PAST, FUTURE);

        //then
        assertTrue(validity.isValidAt(NOW));
        assertTrue(validity.isValidAt(PAST));
        assertFalse(validity.isValidAt(FUTURE)); // exclusive
    }

    @Test
    void is_not_valid_at_instant_before_valid_from() {
        //given
        Validity validity = Validity.between(NOW, FUTURE);

        //then
        assertFalse(validity.isValidAt(PAST));
    }

    @Test
    void is_not_valid_at_instant_after_valid_to() {
        //given
        Validity validity = Validity.between(PAST, NOW);

        //then
        assertFalse(validity.isValidAt(FUTURE));
    }

    @Test
    void is_always_valid_when_created_as_always() {
        //given
        Validity validity = Validity.always();

        //then
        assertTrue(validity.isValidAt(Instant.EPOCH));
        assertTrue(validity.isValidAt(PAST));
        assertTrue(validity.isValidAt(NOW));
        assertTrue(validity.isValidAt(FUTURE));
        assertTrue(validity.isValidAt(FOREVER));
    }

    @Test
    void is_valid_from_specified_date_onwards() {
        //given
        Validity validity = Validity.from(NOW);

        //then
        assertFalse(validity.isValidAt(PAST));
        assertTrue(validity.isValidAt(NOW));
        assertTrue(validity.isValidAt(FUTURE));
        assertTrue(validity.isValidAt(FOREVER));
    }

    @Test
    void is_valid_until_specified_date() {
        //given
        Validity validity = Validity.until(NOW);

        //then
        assertTrue(validity.isValidAt(PAST));
        assertFalse(validity.isValidAt(NOW)); // exclusive
        assertFalse(validity.isValidAt(FUTURE));
        assertTrue(validity.isValidAt(Instant.EPOCH));
    }

    @Test
    void has_not_expired_when_always_valid() {
        //given
        Validity validity = Validity.always();

        //then
        assertFalse(validity.hasExpired(NOW));
        assertFalse(validity.hasExpired(FUTURE));
        assertFalse(validity.hasExpired(FOREVER));
    }

    @Test
    void has_not_expired_when_instant_is_before_valid_to() {
        //given
        Validity validity = Validity.until(FUTURE);

        //then
        assertFalse(validity.hasExpired(PAST));
        assertFalse(validity.hasExpired(NOW));
        assertTrue(validity.hasExpired(FUTURE)); // exclusive
    }

    @Test
    void has_expired_when_instant_is_after_valid_to() {
        //given
        Validity validity = Validity.until(NOW);

        //then
        assertTrue(validity.hasExpired(FUTURE));
        assertTrue(validity.hasExpired(NOW)); // exclusive
        assertFalse(validity.hasExpired(PAST));
    }

    @Test
    void handles_instant_equal_to_valid_from_and_valid_to() {
        //given
        Validity validity = Validity.between(NOW, NOW);

        //then
        assertFalse(validity.isValidAt(NOW)); // empty range [NOW, NOW)
        assertFalse(validity.isValidAt(PAST));
        assertFalse(validity.isValidAt(FUTURE));
    }

    @Test
    void handles_edge_case_with_min_instant() {
        //given
        Validity validity = Validity.from(Instant.EPOCH);

        //then
        assertTrue(validity.isValidAt(Instant.EPOCH));
        assertTrue(validity.isValidAt(NOW));
        assertTrue(validity.isValidAt(FOREVER));
    }

    @Test
    void handles_edge_case_with_max_instant() {
        //given
        Validity validity = Validity.until(Instant.MAX);

        //then
        assertTrue(validity.isValidAt(Instant.EPOCH));
        assertTrue(validity.isValidAt(NOW));
        assertFalse(validity.isValidAt(Instant.MAX)); // exclusive
    }

    @Test
    void handles_edge_case_with_epoch_to_max() {
        //given
        Validity validity = Validity.between(Instant.EPOCH, Instant.MAX);

        //then
        assertTrue(validity.isValidAt(Instant.EPOCH));
        assertTrue(validity.isValidAt(NOW));
        assertFalse(validity.isValidAt(Instant.MAX));
    }

    @Test
    void never_expires_when_valid_from_specified() {
        //given
        Validity validity = Validity.from(PAST);

        //then
        assertFalse(validity.hasExpired(NOW));
        assertFalse(validity.hasExpired(FUTURE));
        assertFalse(validity.hasExpired(FOREVER));
    }

    @Test
    void expires_exactly_at_valid_to() {
        //given
        Validity validity = Validity.until(NOW);

        //then
        assertFalse(validity.hasExpired(PAST));
        assertTrue(validity.hasExpired(NOW)); // exclusive
        assertTrue(validity.hasExpired(FUTURE));
    }

    @Test
    void is_valid_exactly_one_nanosecond_before_valid_to() {
        //given
        Instant oneNanosecondBefore = NOW.minusNanos(1);
        Validity validity = Validity.between(PAST, NOW);

        //then
        assertTrue(validity.isValidAt(oneNanosecondBefore));
        assertFalse(validity.isValidAt(NOW)); // exclusive
        assertFalse(validity.isValidAt(NOW.plusNanos(1)));
    }
}
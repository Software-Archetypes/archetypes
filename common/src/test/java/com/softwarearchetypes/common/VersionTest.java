package com.softwarearchetypes.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class VersionTest {

    @Test
    void shouldCreateInitialVersionWithZeroValue() {
        //when
        Version version = Version.initial();

        //then
        assertNotNull(version);
        assertEquals(0L, version.value());
    }

    @Test
    void shouldCreateVersionWithSpecificValue() {
        //given
        long value = 42L;

        //when
        Version version = Version.of(value);

        //then
        assertNotNull(version);
        assertEquals(value, version.value());
    }

    @Test
    void shouldCreateVersionWithZeroValue() {
        //given
        long value = 0L;

        //when
        Version version = Version.of(value);

        //then
        assertEquals(0L, version.value());
    }

    @Test
    void shouldCreateVersionWithNegativeValue() {
        //given
        long value = -1L;

        //when
        Version version = Version.of(value);

        //then
        assertEquals(value, version.value());
    }

    @Test
    void shouldCreateVersionWithMaxLongValue() {
        //given
        long value = Long.MAX_VALUE;

        //when
        Version version = Version.of(value);

        //then
        assertEquals(value, version.value());
    }

    @Test
    void shouldCreateVersionWithMinLongValue() {
        //given
        long value = Long.MIN_VALUE;

        //when
        Version version = Version.of(value);

        //then
        assertEquals(value, version.value());
    }

    @Test
    void shouldBeEqualWhenVersionsHaveSameValue() {
        //given
        Version firstVersion = Version.of(10L);
        Version secondVersion = Version.of(10L);

        //when & then
        assertEquals(firstVersion, secondVersion);
        assertEquals(firstVersion.hashCode(), secondVersion.hashCode());
    }

    @Test
    void shouldNotBeEqualWhenVersionsHaveDifferentValues() {
        //given
        Version firstVersion = Version.of(10L);
        Version secondVersion = Version.of(20L);

        //when & then
        assertNotEquals(firstVersion, secondVersion);
    }

    @Test
    void shouldHaveProperToStringRepresentation() {
        //given
        long value = 123L;
        Version version = Version.of(value);

        //when
        String result = version.toString();

        //then
        assertNotNull(result);
        assertEquals("Version[value=123]", result);
    }

    @Test
    void shouldInitialVersionBeEqualToVersionOfZero() {
        //given
        Version initialVersion = Version.initial();
        Version zeroVersion = Version.of(0L);

        //when & then
        assertEquals(initialVersion, zeroVersion);
    }

    @Test
    void shouldCreateMultipleInitialVersionsWithSameValue() {
        //given
        Version firstInitial = Version.initial();
        Version secondInitial = Version.initial();

        //when & then
        assertEquals(firstInitial, secondInitial);
        assertEquals(0L, firstInitial.value());
        assertEquals(0L, secondInitial.value());
    }
}

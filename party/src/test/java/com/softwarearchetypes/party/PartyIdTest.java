package com.softwarearchetypes.party;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartyIdTest {

    @Test
    void twoPartyIdsShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        PartyId firstPartyId = PartyId.of(UUID.randomUUID());
        PartyId secondPartyId = PartyId.of(UUID.randomUUID());

        //expect
        assertNotEquals(firstPartyId, secondPartyId);
    }

    @Test
    void twoPartyIdsShouldBeEqualWhenCreatedForTheSameValue() {
        //given
        UUID value = UUID.randomUUID();

        //expect
        assertEquals(PartyId.of(value), PartyId.of(value));
    }

    @Test
    void partyIdIsConvertibleToTheValueItWasCreatedFrom() {
        //given
        UUID value = UUID.randomUUID();
        PartyId partyId = PartyId.of(value);

        //expect
        assertEquals(value.toString(), partyId.asString());
    }

    @Test
    void shouldNotAllowToCreatePartyIdForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PartyId.of(null));
    }

}
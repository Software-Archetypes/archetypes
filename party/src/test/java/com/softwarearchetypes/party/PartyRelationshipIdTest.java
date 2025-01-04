package com.softwarearchetypes.party;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PartyRelationshipIdTest {

    @Test
    void twoPartyRelationshipIdsShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        PartyRelationshipId firstPartyRelationshipId = PartyRelationshipId.of(UUID.randomUUID());
        PartyRelationshipId secondPartyRelationshipId = PartyRelationshipId.of(UUID.randomUUID());

        //expect
        assertNotEquals(firstPartyRelationshipId, secondPartyRelationshipId);
    }

    @Test
    void twoPartyRelationshipIdsShouldBeEqualWhenCreatedForTheSameValue() {
        //given
        UUID value = UUID.randomUUID();

        //expect
        assertEquals(PartyRelationshipId.of(value), PartyRelationshipId.of(value));
    }

    @Test
    void partyRelationshipIdIsConvertibleToTheValueItWasCreatedFrom() {
        //given
        UUID value = UUID.randomUUID();
        PartyRelationshipId partyRelationshipId = PartyRelationshipId.of(value);

        //expect
        assertEquals(value.toString(), partyRelationshipId.asString());
    }

    @Test
    void shouldNotAllowToCreatePartyRelationshipIdForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> PartyRelationshipId.of(null));
    }

}
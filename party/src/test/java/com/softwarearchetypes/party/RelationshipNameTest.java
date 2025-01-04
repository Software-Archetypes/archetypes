package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static com.softwarearchetypes.party.RelationshipNameFixture.someRelationshipNameValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RelationshipNameTest {

    @Test
    void twoRelationshipsShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        RelationshipName firstRelationshipName = RelationshipName.of(someRelationshipNameValue());
        RelationshipName secondRelationshipName = RelationshipName.of(someRelationshipNameValue());

        //expect
        assertNotEquals(firstRelationshipName, secondRelationshipName);
    }

    @Test
    void twoRelationshipsShouldBeEqualWhenCreatedForTheSameValue() {
        //given
        String value = someRelationshipNameValue();

        //expect
        assertEquals(RelationshipName.of(value), RelationshipName.of(value));
    }

    @Test
    void relationshipNameIsConvertibleToTextualValueItWasCreatedFrom() {
        //given
        String value = someRelationshipNameValue();
        RelationshipName relationshipName = RelationshipName.of(value);

        //expect
        assertEquals(value, relationshipName.asString());
    }

    @Test
    void shouldNotAllowToCreateRelationshipNameForNullValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> RelationshipName.of(null));
    }

    @Test
    void shouldNotAllowToCreateRelationshipNameForEmptyValue() {
        //expect
        assertThrows(IllegalArgumentException.class, () -> RelationshipName.of(""));
    }
}
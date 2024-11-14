package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import static com.softwarearchetypes.party.PersonalDataFixture.someFirstName;
import static com.softwarearchetypes.party.PersonalDataFixture.someLastName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonalDataTest {

    @Test
    void twoPersonalDataObjectsShouldNotBeEqualWhenCreatedForDifferentValues() {
        //given
        PersonalData first = PersonalData.from(someFirstName(), someLastName());
        PersonalData second = PersonalData.from(someFirstName(), someLastName());

        //expect
        assertNotEquals(first, second);
    }

    @Test
    void twoPersonalDataObjectsShouldBeEqualWhenCreatedForTheSameValue() {
        //given
        String firstName = someFirstName();
        String lastName = someLastName();

        //expect
        assertEquals(PersonalData.from(firstName, lastName), PersonalData.from(firstName, lastName));
    }

    @Test
    void personalDataIsConvertibleToTextualValuesItWasCreatedFrom() {
        //given
        String firstName = someFirstName();
        String lastName = someLastName();
        PersonalData personalData = PersonalData.from(firstName, lastName);

        //expect
        assertEquals(firstName, personalData.firstName());
        assertEquals(lastName, personalData.lastName());
    }

    @Test
    void shouldCreatePersonalDataWithEmptyValuesForNullValues() {
        //given
        PersonalData personalData = PersonalData.from(null, null);

        //expect
        assertTrue(personalData.firstName().isBlank());
        assertTrue(personalData.lastName().isBlank());
    }

}
package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.PersonalDataUpdateFailed;
import com.softwarearchetypes.party.events.PersonalDataUpdateSkipped;
import com.softwarearchetypes.party.events.PersonalDataUpdated;

import static com.softwarearchetypes.party.PartyFixture.somePerson;
import static com.softwarearchetypes.party.PersonalDataFixture.somePersonalData;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonTest {

    @Test
    void shouldAddPersonalDataToPerson() {
        //given
        Person person = somePerson().withRandomPartyId().build();

        //and
        PersonalData personalData = somePersonalData();

        //when
        Result<PersonalDataUpdateFailed, Person> result = person.update(personalData);

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldReturnPersonalData() {
        //given
        Person person = somePerson().withRandomPartyId().build();

        //and
        PersonalData personalData = somePersonalData();

        //when
        person.update(personalData);

        //then
        assertEquals(personalData, person.personalData());
    }

    @Test
    void shouldGeneratePersonalDataUpdatedEventWhenSuccessfullyDefiningData() {
        //given
        Person person = somePerson().withRandomPartyId().build();

        //and
        PersonalData personalData = somePersonalData();

        //and
        PersonalDataUpdated expectedEvent = new PersonalDataUpdated(person.id().asString(), personalData.firstName(), personalData.lastName());

        //when
        person.update(personalData);

        //then
        assertTrue(person.events().contains(expectedEvent));
    }

    @Test
    void shouldGeneratePersonalDataUpdateSkippedEventWhenNoChangesAreIdentified() {
        //given
        PersonalData data = somePersonalData();
        Person person = ((PersonTestDataBuilder) somePerson().withRandomPartyId()).with(data).build();
        PersonalDataUpdateSkipped expectedEvent = PersonalDataUpdateSkipped.dueToNoChangeIdentifiedFor(person.id()
                                                                                                             .asString(), data.firstName(), data.lastName());

        //when
        person.update(data);

        //then
        assertTrue(person.events().contains(expectedEvent));
    }


}
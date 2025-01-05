package com.softwarearchetypes.party;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdded;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdditionFailed;
import com.softwarearchetypes.party.events.RegisteredIdentifierAdditionSkipped;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemovalFailed;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemovalSkipped;
import com.softwarearchetypes.party.events.RegisteredIdentifierRemoved;

import static com.softwarearchetypes.party.PartyFixture.somePartyOfType;
import static com.softwarearchetypes.party.RegisteredIdentifierFixture.someRegisteredIdentifier;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class PartyRegisteredIdentifiersTest<T extends Party> {

    Class<T> supportedClass;

    PartyRegisteredIdentifiersTest(Class<T> supportedClass) {
        this.supportedClass = supportedClass;
    }

    @Test
    void shouldAddRegisteredIdentifierToTheParty() {
        //given
        T party = somePartyOfType(supportedClass).withRandomPartyId().build();

        //and
        RegisteredIdentifier id = someRegisteredIdentifier();

        //when
        Result<RegisteredIdentifierAdditionFailed, Party> result = party.add(id);

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldReturnAddedRegisteredIdentifier() {
        //given
        T party = somePartyOfType(supportedClass).withRandomPartyId().build();

        //and
        RegisteredIdentifier id = someRegisteredIdentifier();

        //when
        party.add(id);

        //then
        assertTrue(party.registeredIdentifiers().contains(id));
    }

    @Test
    void shouldGenerateRegisteredIdentifierAddedEventWhenSuccessfullyAddingRegisteredIdentifier() {
        //given
        T party = somePartyOfType(supportedClass).withRandomPartyId().build();

        //and
        RegisteredIdentifier id = someRegisteredIdentifier();
        RegisteredIdentifierAdded expectedEvent = new RegisteredIdentifierAdded(party.id().asString(), id.type(), id.asString());

        //when
        party.add(id);

        //then
        assertTrue(party.events().contains(expectedEvent));
    }

    @Test
    void shouldGenerateRegisteredIdentifierAdditionSkippedEventWhenAddingAlreadyExistingRegisteredIdentifier() {
        //given
        RegisteredIdentifier id = someRegisteredIdentifier();
        T party = somePartyOfType(supportedClass).withRandomPartyId().with(id).build();
        RegisteredIdentifierAdditionSkipped expectedEvent = RegisteredIdentifierAdditionSkipped.dueToDataDuplicationFor(party.id()
                                                                                                                             .asString(), id.type(), id.asString());

        //when
        party.add(id);

        //then
        assertTrue(party.events().contains(expectedEvent));
    }

    @Test
    void shouldRemoveRegisteredIdentifierFromParty() {
        //given
        RegisteredIdentifier id = someRegisteredIdentifier();
        T party = somePartyOfType(supportedClass).withRandomPartyId().with(id).build();

        //when
        Result<RegisteredIdentifierRemovalFailed, Party> result = party.remove(id);

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldNotReturnRemovedRegisteredIdentifier() {
        //given
        RegisteredIdentifier id = someRegisteredIdentifier();
        T party = somePartyOfType(supportedClass).withRandomPartyId().with(id).build();

        //when
        party.remove(id);

        //then
        assertFalse(party.registeredIdentifiers().contains(id));
    }

    @Test
    void shouldGenerateRegisteredIdentifierRemovedEventWhenSuccessfullyAddingRegisteredIdentifier() {
        //given
        RegisteredIdentifier id = someRegisteredIdentifier();
        T party = somePartyOfType(supportedClass).withRandomPartyId().with(id).build();
        RegisteredIdentifierRemoved expectedEvent = new RegisteredIdentifierRemoved(party.id().asString(), id.type(), id.asString());

        //when
        party.remove(id);

        //then
        assertTrue(party.events().contains(expectedEvent));
    }

    @Test
    void shouldGenerateRegisteredIdentifierRemovalSkippedEventWhenRemovingNonExistingRegisteredIdentifier() {
        //given
        T party = somePartyOfType(supportedClass).withRandomPartyId().build();

        //and
        RegisteredIdentifier idToBeDeleted = someRegisteredIdentifier();
        RegisteredIdentifierRemovalSkipped expectedEvent = RegisteredIdentifierRemovalSkipped.dueToMissingIdentifierFor(party.id()
                                                                                                                             .asString(), idToBeDeleted.type(), idToBeDeleted.asString());

        //when
        party.remove(idToBeDeleted);

        //then
        assertTrue(party.events().contains(expectedEvent));
    }

}
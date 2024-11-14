package com.softwarearchetypes.party;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.RegisteredIdentifierAdded;
import com.softwarearchetypes.events.RegisteredIdentifierAdditionSkipped;
import com.softwarearchetypes.events.RegisteredIdentifierAdditionSucceeded;
import com.softwarearchetypes.events.RegisteredIdentityAdditionFailed;

import static com.softwarearchetypes.common.CollectionFixture.copyAndAdd;
import static com.softwarearchetypes.common.RandomFixture.randomElementOf;
import static com.softwarearchetypes.party.RegisteredIdentifierFixture.someIdentifierSetOfSize;
import static com.softwarearchetypes.party.RegisteredIdentifierFixture.someRegisteredIdentifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class RegisteredIdentifiersTest {

    @Test
    void emptyRegisteredIdentifierAggregatorIsCreatedForNullRegisteredIdentifierSet() {
        //given
        RegisteredIdentifiers identifiers = RegisteredIdentifiers.from(null);

        //expect
        assertEquals(identifiers.asSet(), Set.of());
    }

    @Test
    void emptyRegisteredIdentifierAggregatorIsCreatedForEmptyRegisteredIdentifierSet() {
        //given
        RegisteredIdentifiers identifiers = RegisteredIdentifiers.from(Set.of());

        //expect
        assertEquals(identifiers.asSet(), Set.of());
    }

    @Test
    void registeredIdentifierAggregatorIsConvertibleToTheRegisteredIdentifierSetItWasCreatedFrom() {
        //given
        Set<RegisteredIdentifier> identifierSet = someIdentifierSetOfSize(5);
        RegisteredIdentifiers identifiers = RegisteredIdentifiers.from(identifierSet);

        //expect
        assertEquals(identifiers.asSet(), identifierSet);
    }

    @Test
    void twoRegisteredIdentifierAggregatorsShouldNotBeEqualWhenCreatedForDifferentRegisteredIdentifierSets() {
        //given
        RegisteredIdentifiers firstRegisteredIdentifiers = RegisteredIdentifiers.from(someIdentifierSetOfSize(5));
        RegisteredIdentifiers secondRegisteredIdentifiers = RegisteredIdentifiers.from(someIdentifierSetOfSize(5));

        //expect
        assertNotEquals(firstRegisteredIdentifiers, secondRegisteredIdentifiers);
    }

    @Test
    void twoRegisteredIdentifierAggregatorsShouldBeEqualWhenCreatedForTheSameRegisteredIdentifierSet() {
        //given
        Set<RegisteredIdentifier> identifierSet = someIdentifierSetOfSize(5);

        //expect
        assertEquals(RegisteredIdentifiers.from(identifierSet), RegisteredIdentifiers.from(identifierSet));
    }

    @Test
    void addingExistingRegisteredIdentifierShouldBeSkippedWhenRegisteredIdentifierAlreadyExistsInRegisteredIdentifierAggregator() {
        //given
        Set<RegisteredIdentifier> initialRegisteredIdentifierSet = someIdentifierSetOfSize(5);
        RegisteredIdentifiers identifiers = RegisteredIdentifiers.from(initialRegisteredIdentifierSet);
        RegisteredIdentifier duplicateIdToBeAdded = randomElementOf(initialRegisteredIdentifierSet);

        //when
        Result<RegisteredIdentityAdditionFailed, RegisteredIdentifierAdditionSucceeded> result = identifiers.add(duplicateIdToBeAdded);

        //then
        assertEquals(RegisteredIdentifierAdditionSkipped.dueToDataDuplicationFor(duplicateIdToBeAdded.type(), duplicateIdToBeAdded.asString()), result.getSuccess());
        assertEquals(initialRegisteredIdentifierSet, identifiers.asSet());
    }

    @Test
    void newRegisteredIdentifierShouldBeAddedWithoutModifyingExistingRegisteredIdentifierAggregator() {
        //given
        Set<RegisteredIdentifier> initialRegisteredIdentifierSet = someIdentifierSetOfSize(5);
        RegisteredIdentifiers identifiers = RegisteredIdentifiers.from(initialRegisteredIdentifierSet);

        //and
        RegisteredIdentifier idToBeAdded = someRegisteredIdentifier();
        Set<RegisteredIdentifier> expectedRegisteredIdentifierSet = copyAndAdd(initialRegisteredIdentifierSet, idToBeAdded);

        //when
        Result<RegisteredIdentityAdditionFailed, RegisteredIdentifierAdditionSucceeded> result = identifiers.add(idToBeAdded);

        //then
        assertEquals(new RegisteredIdentifierAdded(idToBeAdded.type(), idToBeAdded.asString()), result.getSuccess());
        assertEquals(expectedRegisteredIdentifierSet, identifiers.asSet());
    }
}
package com.softwarearchetypes.party;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.AddressAdditionFailed;
import com.softwarearchetypes.events.AddressDefinitionFailed;
import com.softwarearchetypes.events.AddressRemovalFailed;
import com.softwarearchetypes.events.AddressRemovalSkipped;
import com.softwarearchetypes.events.AddressUpdateSkipped;

import static com.softwarearchetypes.party.GeoAddressFixture.someGeoAddressFor;
import static com.softwarearchetypes.party.GeoAddressFixture.someGeoAddressWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AddressesTest {

    @Test
    void shouldAddAddressToParty() {
        //given
        PartyId partyId = PartyId.random();
        Address addressToBeAdded = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId);

        //when
        Result<AddressDefinitionFailed, Addresses> result = addresses.addOrUpdate(addressToBeAdded);

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldGenerateProperAddressDefinedEventWhenSuccessfullyAddingAddressToParty() {
        //given
        PartyId partyId = PartyId.random();
        Address addressToBeAdded = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId);

        //when
        addresses.addOrUpdate(addressToBeAdded);

        //then
        assertTrue(addresses.events().contains(addressToBeAdded.toAddressDefinitionSucceededEvent()));
    }

    @Test
    void shouldUpdateAddressForParty() {
        //given
        PartyId partyId = PartyId.random();
        Address addressToBeUpdated = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId).addOrUpdate(addressToBeUpdated).getSuccess();

        //when
        Result<AddressDefinitionFailed, Addresses> result = addresses.addOrUpdate(addressToBeUpdated);

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldGenerateProperAddressUpdatedEventWhenSuccessfullyUpdatingAddressForParty() {
        //given
        PartyId partyId = PartyId.random();
        Address addressToBeUpdated = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId).addOrUpdate(addressToBeUpdated).getSuccess();

        //and
        Address newAddress = someGeoAddressWith(addressToBeUpdated.id(), partyId);

        //when
        addresses.addOrUpdate(newAddress);

        //then
        assertTrue(addresses.events().contains(newAddress.toAddressUpdateSucceededEvent()));
    }

    @Test
    void shouldRemoveAddressFromParty() {
        //given
        PartyId partyId = PartyId.random();
        Address addressToBeRemoved = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId).addOrUpdate(addressToBeRemoved).getSuccess();

        //when
        Result<AddressRemovalFailed, Addresses> result = addresses.removeAddressWith(addressToBeRemoved.id());

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldGenerateProperAddressRemovalEventWhenSuccessfullyRemovingAddressFromParty() {
        //given
        PartyId partyId = PartyId.random();
        Address addressToBeRemoved = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId).addOrUpdate(addressToBeRemoved).getSuccess();

        //when
        addresses.removeAddressWith(addressToBeRemoved.id());

        //then
        assertTrue(addresses.events().contains(addressToBeRemoved.toAddressRemovalSucceededEvent()));
    }

    @Test
    void addressRemovalShouldBeIgnoredWhenItDoesNotExistForParty() {
        //given
        PartyId partyId = PartyId.random();
        Addresses addresses = Addresses.emptyAddressesFor(partyId);

        //when
        Result<AddressRemovalFailed, Addresses> result = addresses.removeAddressWith(AddressId.random());

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldGenerateProperAddressRemovalSkippedEventWhenAddressDoesNotExistForParty() {
        //given
        PartyId partyId = PartyId.random();
        Addresses addresses = Addresses.emptyAddressesFor(partyId);
        AddressId notExistingAddressId = AddressId.random();

        //when
        addresses.removeAddressWith(notExistingAddressId);

        //then
        assertTrue(addresses.events().contains(AddressRemovalSkipped.dueToAddressNotFoundFor(notExistingAddressId.asString(), partyId.asString())));
    }

    @Test
    void addressUpdateShouldBeIgnoredWhenNoChangesHasBeenFound() {
        //given
        PartyId partyId = PartyId.random();
        Address address = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId).addOrUpdate(address).getSuccess();

        //when
        Result<AddressDefinitionFailed, Addresses> result = addresses.addOrUpdate(address);

        //then
        assertTrue(result.success());
    }

    @Test
    void shouldGenerateProperAddressUpdateSkippedEventWhenNoChangesHasBeenFound() {
        //given
        PartyId partyId = PartyId.random();
        Address address = someGeoAddressFor(partyId);
        Addresses addresses = Addresses.emptyAddressesFor(partyId).addOrUpdate(address).getSuccess();

        //when
        addresses.addOrUpdate(address);

        //then
        assertTrue(addresses.events().contains(AddressUpdateSkipped.dueToNoChangesIdentifiedFor(address.id().asString(), partyId.asString())));
    }

    @Test
    void givenThereIsPolicyLimitingAddressCountAddingNewAddressShouldFail() {
        //given
        PartyId partyId = PartyId.random();
        Address address = someGeoAddressFor(partyId);
        AddressDefiningPolicy addressCountLimitingPolicy = ((ads, ad) -> ads.asSet().isEmpty());
        Addresses addresses = Addresses.emptyAddressesFor(partyId, addressCountLimitingPolicy).addOrUpdate(address).getSuccess();

        //and
        Address newAddress = someGeoAddressFor(partyId);

        //when
        Result<AddressDefinitionFailed, Addresses> result = addresses.addOrUpdate(newAddress);

        //then
        assertEquals(AddressAdditionFailed.dueToPolicyNotMetFor(newAddress.id().asString(), partyId.asString()), result.getFailure());
    }

    @Test
    @Disabled("ignored until other address types occur")
    void geoAddressUpdateShouldFailWhenUpdatingWithAddressOfDifferentType() {
    }

}
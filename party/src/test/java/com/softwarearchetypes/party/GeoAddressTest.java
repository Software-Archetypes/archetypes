package com.softwarearchetypes.party;

import java.util.Set;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import com.softwarearchetypes.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.events.AddressRemovalSucceeded;
import com.softwarearchetypes.events.AddressUpdateSucceeded;
import com.softwarearchetypes.events.GeoAddressDefined;
import com.softwarearchetypes.events.GeoAddressRemoved;
import com.softwarearchetypes.events.GeoAddressUpdated;

import static com.softwarearchetypes.common.CollectionFixture.stringSetFrom;
import static com.softwarearchetypes.party.GeoAddressFixture.someGeoAddressDetails;
import static com.softwarearchetypes.party.GeoAddressFixture.someUseTypes;
import static com.softwarearchetypes.party.GeoAddressFixture.someUseTypesDifferentThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class GeoAddressTest {

    @Test
    void geoAddressShouldContainDataPassedWhenCreatingIt() {
        //given
        AddressId addressId = AddressId.random();
        PartyId partyId = PartyId.random();
        GeoAddress.GeoAddressDetails geoAddressDetails = someGeoAddressDetails();
        Set<AddressUseType> useTypes = someUseTypes();

        //and
        GeoAddress address = new GeoAddress(addressId, partyId, geoAddressDetails, useTypes);

        //expect
        assertEquals(addressId, address.id());
        assertEquals(partyId, address.partyId());
        assertEquals(geoAddressDetails, address.addressDetails());
        assertEquals(useTypes, address.useTypes());
    }

    @Test
    void shouldCreateAddressDefinitionSucceededEventFromGeoAddress() {
        //given
        GeoAddress address = new GeoAddress(AddressId.random(), PartyId.random(), someGeoAddressDetails(), someUseTypes());

        //when
        AddressDefinitionSucceeded event = address.toAddressDefinitionSucceededEvent();

        //then
        assertEquals(event, geoAddressDefinedEventFor(address));
    }

    @Test
    void shouldCreateAddressRemovalSucceededEventFromGeoAddress() {
        //given
        GeoAddress address = new GeoAddress(AddressId.random(), PartyId.random(), someGeoAddressDetails(), someUseTypes());

        //when
        AddressRemovalSucceeded event = address.toAddressRemovalSucceededEvent();

        //then
        assertEquals(event, geoAddressRemovedEventFor(address));
    }

    @Test
    void shouldCreateAddressUpdateSucceededEventFromGeoAddress() {
        //given
        GeoAddress address = new GeoAddress(AddressId.random(), PartyId.random(), someGeoAddressDetails(), someUseTypes());

        //when
        AddressUpdateSucceeded event = address.toAddressUpdateSucceededEvent();

        //then
        assertEquals(event, geoAddressUpdatedEventFrom(address.id(), address.partyId(), address));
    }

    @Test
    void twoGeoAddressObjectShouldNotBeEqualWhenHavingDifferentIds() {
        //given
        PartyId partyId = PartyId.random();
        GeoAddress.GeoAddressDetails geoAddressDetails = someGeoAddressDetails();
        Set<AddressUseType> useTypes = someUseTypes();

        //and
        GeoAddress first = new GeoAddress(AddressId.random(), partyId, geoAddressDetails, useTypes);
        GeoAddress second = new GeoAddress(AddressId.random(), partyId, geoAddressDetails, useTypes);

        //expect
        assertNotEquals(first, second);
    }

    @Test
    void twoGeoAddressObjectShouldNotBeEqualWhenBelongingToDifferentParties() {
        //given
        AddressId addressId = AddressId.random();
        GeoAddress.GeoAddressDetails geoAddressDetails = someGeoAddressDetails();
        Set<AddressUseType> useTypes = someUseTypes();

        //and
        GeoAddress first = new GeoAddress(addressId, PartyId.random(), geoAddressDetails, useTypes);
        GeoAddress second = new GeoAddress(addressId, PartyId.random(), geoAddressDetails, useTypes);

        //expect
        assertNotEquals(first, second);
    }

    @Test
    void twoGeoAddressObjectShouldNotBeEqualWhenDetailsAreDifferent() {
        //given
        AddressId addressId = AddressId.random();
        PartyId partyId = PartyId.random();
        Set<AddressUseType> useTypes = someUseTypes();

        //and
        GeoAddress first = new GeoAddress(addressId, partyId, someGeoAddressDetails(), useTypes);
        GeoAddress second = new GeoAddress(addressId, partyId, someGeoAddressDetails(), useTypes);

        //expect
        assertNotEquals(first, second);
    }

    @Test
    void twoGeoAddressObjectShouldNotBeEqualWhenUseTypesAreDifferent() {
        //given
        AddressId addressId = AddressId.random();
        PartyId partyId = PartyId.random();
        GeoAddress.GeoAddressDetails details = someGeoAddressDetails();
        Set<AddressUseType> useTypes = someUseTypes();

        //and
        GeoAddress first = new GeoAddress(addressId, partyId, details, useTypes);
        GeoAddress second = new GeoAddress(addressId, partyId, details, someUseTypesDifferentThan(useTypes));

        //expect
        assertNotEquals(first, second);
    }

    @Test
    void twoGeoAddressObjectShouldBeEqualWhenHavingTheSameValues() {
        //given
        AddressId id = AddressId.random();
        PartyId partyId = PartyId.random();
        GeoAddress.GeoAddressDetails details = someGeoAddressDetails();
        Set<AddressUseType> useTypes = someUseTypes();

        //and
        GeoAddress first = new GeoAddress(id, partyId, details, useTypes);
        GeoAddress second = new GeoAddress(id, partyId, details, useTypes);

        //expect
        assertEquals(first, second);
    }

    @NotNull
    private static GeoAddressUpdated geoAddressUpdatedEventFrom(AddressId id, PartyId partyId, GeoAddress newAddress) {
        return new GeoAddressUpdated(id.asString(), partyId.asString(), newAddress.name(), newAddress.street(), newAddress.building(),
                newAddress.flat(), newAddress.city(), newAddress.zip().asString(), newAddress.locale().toString(),
                newAddress.useTypes().stream().map(Enum::toString).collect(Collectors.toSet()));
    }

    private static GeoAddressDefined geoAddressDefinedEventFor(GeoAddress address) {
        return new GeoAddressDefined(address.id().asString(), address.partyId().asString(),
                address.name(), address.street(), address.building(), address.flat(), address.city(),
                address.zip().asString(), address.locale().toString(), stringSetFrom(address.useTypes()));
    }

    private static GeoAddressRemoved geoAddressRemovedEventFor(GeoAddress address) {
        return new GeoAddressRemoved(address.id().asString(), address.partyId().asString());
    }
}
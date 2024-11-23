package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.softwarearchetypes.common.RandomFixture.randomElementOf;
import static com.softwarearchetypes.common.RandomFixture.randomStringWithPrefixOf;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

class GeoAddressFixture {

    static GeoAddress someGeoAddressFor(PartyId partyId) {
        return someGeoAddressWith(AddressId.random(), partyId);
    }

    static GeoAddress someGeoAddressWith(AddressId addressId, PartyId partyId) {
        return new GeoAddress(addressId, partyId, someGeoAddressDetails(), someUseTypes());
    }

    static GeoAddress.GeoAddressDetails someGeoAddressDetails() {
        return GeoAddress.GeoAddressDetails.from(someName(), someStreet(), someBuilding(), someFlat(), someCity(), someZipCode(), someLocale());
    }

    static String someName() {
        return randomStringWithPrefixOf("name");
    }

    static String someStreet() {
        return randomStringWithPrefixOf("street");
    }

    static String someBuilding() {
        return randomNumeric(3);
    }

    static String someFlat() {
        return randomNumeric(3);
    }

    static String someCity() {
        return randomStringWithPrefixOf("city");
    }

    static ZipCode someZipCode() {
        return ZipCode.of(randomNumeric(2) + "-" + randomNumeric(3));
    }

    static Locale someLocale() {
        return Locale.getDefault();
    }

    static AddressUseType someUseType() {
        return randomElementOf(List.of(AddressUseType.values()));
    }

    static Set<AddressUseType> someUseTypes() {
        Set<AddressUseType> useTypes = new HashSet<>();
        useTypes.add(someUseType());
        useTypes.add(someUseType());
        useTypes.add(someUseType());
        return useTypes;
    }

    static Set<AddressUseType> someUseTypesDifferentThan(Set<AddressUseType> useTypes) {
        Set<AddressUseType> newUseTypes = someUseTypes();
        while(newUseTypes.equals(useTypes)) {
            newUseTypes = someUseTypes();
        }
        return newUseTypes;
    }

}

package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.softwarearchetypes.party.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.party.events.AddressRemovalSucceeded;
import com.softwarearchetypes.party.events.AddressUpdateSucceeded;
import com.softwarearchetypes.party.events.GeoAddressDefined;
import com.softwarearchetypes.party.events.GeoAddressRemoved;
import com.softwarearchetypes.party.events.GeoAddressUpdated;

public final class GeoAddress implements Address {

    private final AddressId id;
    private final PartyId partyId;
    private final GeoAddressDetails geoAddressDetails;
    private final Set<AddressUseType> useTypes;

    GeoAddress(AddressId id, PartyId partyId, GeoAddressDetails geoAddressDetails, Set<AddressUseType> useTypes) {
        this.id = id;
        this.partyId = partyId;
        this.geoAddressDetails = geoAddressDetails;
        this.useTypes = Optional.ofNullable(useTypes).map(HashSet::new).orElse(new HashSet<>());
    }

    String name() {
        return geoAddressDetails.name();
    }

    String street() {
        return geoAddressDetails.street();
    }

    String building() {
        return geoAddressDetails.building();
    }

    String flat() {
        return geoAddressDetails.flat();
    }

    String city() {
        return geoAddressDetails.city();
    }

    ZipCode zip() {
        return geoAddressDetails.zip();
    }

    Locale locale() {
        return geoAddressDetails.locale();
    }

    @Override
    public AddressId id() {
        return id;
    }

    @Override
    public PartyId partyId() {
        return partyId;
    }

    @Override
    public Set<AddressUseType> useTypes() {
        return new HashSet<>(useTypes);
    }

    @Override
    public AddressDetails addressDetails() {
        return geoAddressDetails;
    }

    @Override
    public AddressUpdateSucceeded toAddressUpdateSucceededEvent() {
        return new GeoAddressUpdated(id.asString(), partyId.asString(), geoAddressDetails.name(),
                geoAddressDetails.street(), geoAddressDetails.building(), geoAddressDetails.flat(),
                geoAddressDetails.city(), geoAddressDetails.zip().asString(), geoAddressDetails.locale().toString(),
                useTypesAsStringSet());
    }

    @Override
    public AddressDefinitionSucceeded toAddressDefinitionSucceededEvent() {
        return new GeoAddressDefined(id.asString(), partyId.asString(), geoAddressDetails.name(), geoAddressDetails.street(), geoAddressDetails.building(),
                geoAddressDetails.flat(), geoAddressDetails.city(), geoAddressDetails.zip().asString(), geoAddressDetails.locale()
                                                                                                                         .toString(), useTypesAsStringSet());
    }

    @Override
    public AddressRemovalSucceeded toAddressRemovalSucceededEvent() {
        return new GeoAddressRemoved(id.asString(), partyId.asString());
    }

    private Set<String> useTypesAsStringSet() {
        return this.useTypes.stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof GeoAddress that)) {
            return false;
        }
        return Objects.equals(id, that.id) && Objects.equals(partyId, that.partyId) && Objects.equals(geoAddressDetails, that.geoAddressDetails) && Objects.equals(useTypes, that.useTypes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, partyId, geoAddressDetails, useTypes);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", GeoAddress.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("partyId=" + partyId)
                .add("geoAddressDetails=" + geoAddressDetails)
                .add("useTypes=" + useTypes)
                .toString();
    }

    public record GeoAddressDetails(String name, String street, String building, String flat, String city, ZipCode zip,
                                    Locale locale) implements AddressDetails {

        static GeoAddressDetails from(String name, String street, String building, String flat, String city, ZipCode zip, Locale locale) {
            return new GeoAddressDetails(name, street, building, flat, city, zip, locale);
        }
    }
}

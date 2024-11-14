package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.events.AddressRemovalSucceeded;
import com.softwarearchetypes.events.AddressUpdateFailed;
import com.softwarearchetypes.events.AddressUpdateSucceeded;
import com.softwarearchetypes.events.GeoAddressDefined;
import com.softwarearchetypes.events.GeoAddressRemoved;
import com.softwarearchetypes.events.GeoAddressUpdateSkipped;
import com.softwarearchetypes.events.GeoAddressUpdated;

public final class GeoAddress implements Address {

    private final AddressId id;
    private final PartyId partyId;
    private String name;
    private String companyName;
    private String street;
    private String building;
    private String flat;
    private String city;
    private ZipCode zip;
    private Locale locale;
    private Set<AddressUseType> useTypes;

    GeoAddress(AddressId id, PartyId partyId, String name, String companyName, String street, String building, String flat, String city, ZipCode zip, Locale locale, Set<AddressUseType> useTypes) {
        this.id = id;
        this.partyId = partyId;
        this.name = name;
        this.companyName = companyName;
        this.street = street;
        this.building = building;
        this.flat = flat;
        this.city = city;
        this.zip = zip;
        this.locale = locale;
        this.useTypes = Optional.ofNullable(useTypes).map(HashSet::new).orElse(new HashSet<>());
    }

    String name() {
        return name;
    }

    String companyName() {
        return companyName;
    }

    String street() {
        return street;
    }

    String building() {
        return building;
    }

    String flat() {
        return flat;
    }

    String city() {
        return city;
    }

    ZipCode zip() {
        return zip;
    }

    Locale locale() {
        return locale;
    }

    @Override
    public AddressId id() {
        return id;
    }

    @Override
    public Set<AddressUseType> useTypes() {
        return new HashSet<>(useTypes);
    }

    @Override
    public Result<AddressUpdateFailed, AddressUpdateSucceeded> updateWith(Address address) {
        if (address instanceof GeoAddress) {
            if (thereIsAnyChangeIn((GeoAddress) address)) {
                applyChangesFrom((GeoAddress) address);
                return Result.success(geoAddressUpdatedEvent());
            } else {
                return Result.success(geoAddressUpdateIgnoredDueToNoChangesIdentified());
            }
        } else {
            return Result.failure(AddressUpdateFailed.dueToNotMatchingAddressType());
        }
    }

    @Override
    public AddressDefinitionSucceeded toAddressDefinitionSucceededEvent() {
        return new GeoAddressDefined(id.asString(), partyId.asString(), name, companyName, street, building, flat, city, zip.asString(), locale.toString(), useTypesAsStringSet());
    }

    private GeoAddressUpdated geoAddressUpdatedEvent() {
        return new GeoAddressUpdated(id.asString(), partyId.asString(), name, companyName, street, building, flat, city, zip.asString(), locale.toString(), useTypesAsStringSet());
    }

    @Override
    public AddressRemovalSucceeded toAddressRemovalSucceededEvent() {
        return new GeoAddressRemoved(id.asString(), partyId.asString());
    }

    private void applyChangesFrom(GeoAddress address) {
        this.name = address.name();
        this.companyName = address.companyName();
        this.street = address.street();
        this.building = address.building();
        this.flat = address.flat();
        this.city = address.city();
        this.zip = address.zip();
        this.locale = address.locale();
        this.useTypes = address.useTypes();
    }

    private boolean thereIsAnyChangeIn(GeoAddress address) {
        return !isContentEqualTo(address);
    }

    private boolean isContentEqualTo(GeoAddress address) {
        return Objects.equals(this.name, address.name) &&
                Objects.equals(this.companyName, address.companyName) &&
                Objects.equals(this.street, address.street) &&
                Objects.equals(this.building, address.building) &&
                Objects.equals(this.flat, address.flat) &&
                Objects.equals(this.city, address.city) &&
                Objects.equals(this.zip, address.zip) &&
                Objects.equals(this.locale, address.locale);
    }

    private GeoAddressUpdateSkipped geoAddressUpdateIgnoredDueToNoChangesIdentified() {
        return GeoAddressUpdateSkipped.dueToNoChangesIdentifiedFor(id.asString(), partyId.asString());
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
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public sealed abstract class Address implements AddressLifecycle permits GeoAddress, EmailAddress, PhoneAddress, WebAddress {

    private final AddressId id;
    private final PartyId partyId;
    private final Set<AddressUseType> useTypes;
    private final Validity validity;

    protected Address(AddressId id, PartyId partyId, Set<AddressUseType> useTypes) {
        this(id, partyId, useTypes, Validity.ALWAYS);
    }

    protected Address(AddressId id, PartyId partyId, Set<AddressUseType> useTypes, Validity validity) {
        this.id = id;
        this.partyId = partyId;
        this.useTypes = Optional.ofNullable(useTypes).map(HashSet::new).orElse(new HashSet<>());
        this.validity = validity;
    }

    public final AddressId id() {
        return id;
    }

    public final PartyId partyId() {
        return partyId;
    }

    public final Set<AddressUseType> useTypes() {
        return new HashSet<>(useTypes);
    }

    public abstract AddressDetails addressDetails();

    public final Validity validity() {
        return validity;
    }

    public final boolean isCurrentlyValid() {
        return validity.isCurrentlyValid();
    }

    public final boolean isValidAt(java.time.Instant instant) {
        return validity.isValidAt(instant);
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Address address)) {
            return false;
        }
        return Objects.equals(id, address.id) && Objects.equals(partyId, address.partyId);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id, partyId);
    }
}
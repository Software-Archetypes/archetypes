package com.softwarearchetypes.party;

import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.softwarearchetypes.party.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.party.events.AddressRemovalSucceeded;
import com.softwarearchetypes.party.events.AddressUpdateSucceeded;
import com.softwarearchetypes.party.events.PhoneAddressDefined;
import com.softwarearchetypes.party.events.PhoneAddressRemoved;
import com.softwarearchetypes.party.events.PhoneAddressUpdated;

public final class PhoneAddress extends Address {

    private final PhoneAddressDetails phoneAddressDetails;

    PhoneAddress(AddressId id, PartyId partyId, PhoneAddressDetails phoneAddressDetails, Set<AddressUseType> useTypes) {
        super(id, partyId, useTypes);
        this.phoneAddressDetails = phoneAddressDetails;
    }

    PhoneAddress(AddressId id, PartyId partyId, PhoneAddressDetails phoneAddressDetails, Set<AddressUseType> useTypes, Validity validity) {
        super(id, partyId, useTypes, validity);
        this.phoneAddressDetails = phoneAddressDetails;
    }

    @Override
    public AddressDetails addressDetails() {
        return phoneAddressDetails;
    }

    @Override
    public AddressUpdateSucceeded toAddressUpdateSucceededEvent() {
        return new PhoneAddressUpdated(id().asString(), partyId().asString(), phoneAddressDetails.phoneNumber(), useTypesAsStringSet());
    }

    @Override
    public AddressDefinitionSucceeded toAddressDefinitionSucceededEvent() {
        return new PhoneAddressDefined(id().asString(), partyId().asString(), phoneAddressDetails.phoneNumber(), useTypesAsStringSet());
    }

    @Override
    public AddressRemovalSucceeded toAddressRemovalSucceededEvent() {
        return new PhoneAddressRemoved(id().asString(), partyId().asString());
    }

    private Set<String> useTypesAsStringSet() {
        return useTypes().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", PhoneAddress.class.getSimpleName() + "[", "]")
                .add("id=" + id())
                .add("partyId=" + partyId())
                .add("phoneAddressDetails=" + phoneAddressDetails)
                .add("useTypes=" + useTypes())
                .add("validity=" + validity())
                .toString();
    }
}

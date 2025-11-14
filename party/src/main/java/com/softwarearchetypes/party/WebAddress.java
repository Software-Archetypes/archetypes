package com.softwarearchetypes.party;

import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.softwarearchetypes.party.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.party.events.AddressRemovalSucceeded;
import com.softwarearchetypes.party.events.AddressUpdateSucceeded;
import com.softwarearchetypes.party.events.WebAddressDefined;
import com.softwarearchetypes.party.events.WebAddressRemoved;
import com.softwarearchetypes.party.events.WebAddressUpdated;

public final class WebAddress extends Address {

    private final WebAddressDetails webAddressDetails;

    WebAddress(AddressId id, PartyId partyId, WebAddressDetails webAddressDetails, Set<AddressUseType> useTypes) {
        super(id, partyId, useTypes);
        this.webAddressDetails = webAddressDetails;
    }

    WebAddress(AddressId id, PartyId partyId, WebAddressDetails webAddressDetails, Set<AddressUseType> useTypes, Validity validity) {
        super(id, partyId, useTypes, validity);
        this.webAddressDetails = webAddressDetails;
    }

    @Override
    public AddressDetails addressDetails() {
        return webAddressDetails;
    }

    @Override
    public AddressUpdateSucceeded toAddressUpdateSucceededEvent() {
        return new WebAddressUpdated(id().asString(), partyId().asString(), webAddressDetails.url(), useTypesAsStringSet());
    }

    @Override
    public AddressDefinitionSucceeded toAddressDefinitionSucceededEvent() {
        return new WebAddressDefined(id().asString(), partyId().asString(), webAddressDetails.url(), useTypesAsStringSet());
    }

    @Override
    public AddressRemovalSucceeded toAddressRemovalSucceededEvent() {
        return new WebAddressRemoved(id().asString(), partyId().asString());
    }

    private Set<String> useTypesAsStringSet() {
        return useTypes().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", WebAddress.class.getSimpleName() + "[", "]")
                .add("id=" + id())
                .add("partyId=" + partyId())
                .add("webAddressDetails=" + webAddressDetails)
                .add("useTypes=" + useTypes())
                .add("validity=" + validity())
                .toString();
    }
}

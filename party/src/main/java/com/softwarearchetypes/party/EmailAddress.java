package com.softwarearchetypes.party;

import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import com.softwarearchetypes.party.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.party.events.AddressRemovalSucceeded;
import com.softwarearchetypes.party.events.AddressUpdateSucceeded;
import com.softwarearchetypes.party.events.EmailAddressDefined;
import com.softwarearchetypes.party.events.EmailAddressRemoved;
import com.softwarearchetypes.party.events.EmailAddressUpdated;

public final class EmailAddress extends Address {

    private final EmailAddressDetails emailAddressDetails;

    EmailAddress(AddressId id, PartyId partyId, EmailAddressDetails emailAddressDetails, Set<AddressUseType> useTypes) {
        super(id, partyId, useTypes);
        this.emailAddressDetails = emailAddressDetails;
    }

    EmailAddress(AddressId id, PartyId partyId, EmailAddressDetails emailAddressDetails, Set<AddressUseType> useTypes, Validity validity) {
        super(id, partyId, useTypes, validity);
        this.emailAddressDetails = emailAddressDetails;
    }

    @Override
    public AddressDetails addressDetails() {
        return emailAddressDetails;
    }

    @Override
    public AddressUpdateSucceeded toAddressUpdateSucceededEvent() {
        return new EmailAddressUpdated(id().asString(), partyId().asString(), emailAddressDetails.email(), useTypesAsStringSet());
    }

    @Override
    public AddressDefinitionSucceeded toAddressDefinitionSucceededEvent() {
        return new EmailAddressDefined(id().asString(), partyId().asString(), emailAddressDetails.email(), useTypesAsStringSet());
    }

    @Override
    public AddressRemovalSucceeded toAddressRemovalSucceededEvent() {
        return new EmailAddressRemoved(id().asString(), partyId().asString());
    }

    private Set<String> useTypesAsStringSet() {
        return useTypes().stream().map(Enum::name).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", EmailAddress.class.getSimpleName() + "[", "]")
                .add("id=" + id())
                .add("partyId=" + partyId())
                .add("emailAddressDetails=" + emailAddressDetails)
                .add("useTypes=" + useTypes())
                .add("validity=" + validity())
                .toString();
    }
}

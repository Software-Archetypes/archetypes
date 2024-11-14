package com.softwarearchetypes.party;

import java.util.Set;

import com.softwarearchetypes.common.Result;
import com.softwarearchetypes.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.events.AddressRemovalSucceeded;
import com.softwarearchetypes.events.AddressUpdateFailed;
import com.softwarearchetypes.events.AddressUpdateSucceeded;

public sealed interface Address permits GeoAddress {

    AddressId id();

    Set<AddressUseType> useTypes();

    Result<AddressUpdateFailed, AddressUpdateSucceeded> updateWith(Address address);

    AddressDefinitionSucceeded toAddressDefinitionSucceededEvent();

    AddressRemovalSucceeded toAddressRemovalSucceededEvent();
}
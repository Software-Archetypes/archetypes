package com.softwarearchetypes.party;

import com.softwarearchetypes.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.events.AddressRemovalSucceeded;
import com.softwarearchetypes.events.AddressUpdateSucceeded;

interface AddressLifecycle {

    AddressUpdateSucceeded toAddressUpdateSucceededEvent();

    AddressDefinitionSucceeded toAddressDefinitionSucceededEvent();

    AddressRemovalSucceeded toAddressRemovalSucceededEvent();
}
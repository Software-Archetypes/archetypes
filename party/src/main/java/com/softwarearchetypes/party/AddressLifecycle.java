package com.softwarearchetypes.party;

import com.softwarearchetypes.party.events.AddressDefinitionSucceeded;
import com.softwarearchetypes.party.events.AddressRemovalSucceeded;
import com.softwarearchetypes.party.events.AddressUpdateSucceeded;

interface AddressLifecycle {

    AddressUpdateSucceeded toAddressUpdateSucceededEvent();

    AddressDefinitionSucceeded toAddressDefinitionSucceededEvent();

    AddressRemovalSucceeded toAddressRemovalSucceededEvent();
}
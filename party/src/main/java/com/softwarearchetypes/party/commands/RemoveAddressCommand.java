package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.AddressId;
import com.softwarearchetypes.party.PartyId;

public record RemoveAddressCommand(PartyId partyId, AddressId addressId) {
}

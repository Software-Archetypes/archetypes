package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.PartyId;

public record AddOrUpdateGeoAddressCommand(PartyId partyId, GeoAddressDTO address) {
}

package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.PartyId;

public record UpdateOrganizationNameCommand(PartyId partyId, String organizationName) {
}

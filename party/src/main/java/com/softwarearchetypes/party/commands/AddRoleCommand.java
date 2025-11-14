package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.PartyId;

public record AddRoleCommand(PartyId partyId, String role) {
}

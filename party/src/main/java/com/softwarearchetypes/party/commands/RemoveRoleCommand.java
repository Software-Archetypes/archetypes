package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.PartyId;

public record RemoveRoleCommand(PartyId partyId, String role) {
}

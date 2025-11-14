package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.PartyId;

public record UpdatePersonalDataCommand(
        PartyId partyId,
        String firstName,
        String lastName) {
}

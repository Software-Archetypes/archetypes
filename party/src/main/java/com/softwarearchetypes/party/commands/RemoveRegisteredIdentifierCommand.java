package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.PartyId;
import com.softwarearchetypes.party.RegisteredIdentifier;

public record RemoveRegisteredIdentifierCommand(
        PartyId partyId,
        RegisteredIdentifier registeredIdentifier) {
}

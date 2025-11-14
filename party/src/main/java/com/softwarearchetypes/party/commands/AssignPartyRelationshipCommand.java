package com.softwarearchetypes.party.commands;

import com.softwarearchetypes.party.PartyId;

public record AssignPartyRelationshipCommand(
        PartyId fromPartyId,
        String fromRole,
        PartyId toPartyId,
        String toRole,
        String relationshipName) {
}

package com.softwarearchetypes.party;

import com.softwarearchetypes.party.PartyId;
import com.softwarearchetypes.party.PartyRelationshipId;

public record PartyRelationshipView(
        PartyRelationshipId id,
        PartyId fromPartyId,
        String fromRole,
        PartyId toPartyId,
        String toRole,
        String relationshipName,
        Validity validity) {
}

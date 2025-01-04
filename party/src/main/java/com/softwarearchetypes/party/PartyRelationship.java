package com.softwarearchetypes.party;

import com.softwarearchetypes.party.events.PartyRelationshipAdded;

public record PartyRelationship(PartyRelationshipId id, PartyRole from, PartyRole to, RelationshipName name) {

    static PartyRelationship from(PartyRelationshipId id, PartyRole from, PartyRole to, RelationshipName name) {
        return new PartyRelationship(id, from, to, name);
    }

    PartyRelationshipAdded toPartyRelationshipAddedEvent() {
        return new PartyRelationshipAdded(id.asString(), from.partyId().asString(),
                from.role().asString(), to.partyId().asString(), to.role().asString(), name.asString());
    }

}

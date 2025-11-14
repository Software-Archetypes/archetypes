package com.softwarearchetypes.party;

interface PartyRelationshipDefiningPolicy {

    boolean canDefineFor(PartyRole from, PartyRole to, RelationshipName relationshipName);

    static PartyRelationshipDefiningPolicy alwaysAllow() {
        return new AlwaysAllowPartyRelationshipDefiningPolicy();
    }
}

final class AlwaysAllowPartyRelationshipDefiningPolicy implements PartyRelationshipDefiningPolicy {

    @Override
    public boolean canDefineFor(PartyRole from, PartyRole to, RelationshipName relationshipName) {
        return true;
    }
}

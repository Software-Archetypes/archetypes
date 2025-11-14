package com.softwarearchetypes.party;

public class PartyRelationshipViewMapper {

    public static PartyRelationshipView toView(PartyRelationship relationship) {
        return new PartyRelationshipView(
                relationship.id(),
                relationship.from().partyId(),
                relationship.from().role().asString(),
                relationship.to().partyId(),
                relationship.to().role().asString(),
                relationship.name().asString(),
                relationship.validity()
        );
    }
}

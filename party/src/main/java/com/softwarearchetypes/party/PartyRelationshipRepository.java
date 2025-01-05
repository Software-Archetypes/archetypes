package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;

public interface PartyRelationshipRepository {

    List<PartyRelationship> findAllRelationsFrom(PartyId partyId);

    List<PartyRelationship> findAllRelationsFrom(PartyId partyId, RelationshipName name);

    Optional<PartyRelationship> findBy(PartyRelationshipId relationshipId);

    void save(PartyRelationship partyRelationship);

    Optional<PartyRelationshipId> delete(PartyRelationshipId relationshipId);
}

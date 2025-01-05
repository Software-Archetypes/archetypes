package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;

public class PartyRelationshipsQueries {

    private final PartyRelationshipRepository repository;

    PartyRelationshipsQueries(PartyRelationshipRepository partyRepository) {
        this.repository = partyRepository;
    }

    public Optional<PartyRelationship> findBy(PartyRelationshipId partyId) {
        return repository.findBy(partyId);
    }

    public List<PartyRelationship> findAllRelationsFrom(PartyId partyId) {
        return repository.findAllRelationsFrom(partyId);
    }

    public List<PartyRelationship> findAllRelationsFrom(PartyId partyId, RelationshipName name) {
        return repository.findAllRelationsFrom(partyId, name);
    }
}

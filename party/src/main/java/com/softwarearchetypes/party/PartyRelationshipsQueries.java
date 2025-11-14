package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class PartyRelationshipsQueries {

    private final PartyRelationshipRepository repository;

    PartyRelationshipsQueries(PartyRelationshipRepository repository) {
        this.repository = repository;
    }

    public Optional<PartyRelationshipView> findBy(PartyRelationshipId partyId) {
        return repository.findBy(partyId)
                         .map(PartyRelationshipViewMapper::toView);
    }

    public List<PartyRelationshipView> findAllRelationsFrom(PartyId partyId) {
        return repository.findAllRelationsFrom(partyId).stream()
                         .map(PartyRelationshipViewMapper::toView)
                         .toList();
    }

    public List<PartyRelationshipView> findAllRelationsFrom(PartyId partyId, String relationshipName) {
        RelationshipName name = RelationshipName.of(relationshipName);
        return repository.findAllRelationsFrom(partyId, name).stream()
                         .map(PartyRelationshipViewMapper::toView)
                         .toList();
    }

    public List<PartyRelationshipView> findAllRelationsFrom(List<PartyId> partyIds, String relationshipName) {
        RelationshipName name = RelationshipName.of(relationshipName);
        return partyIds.stream()
                       .flatMap(it -> repository.findAllRelationsFrom(it, name).stream())
                       .map(PartyRelationshipViewMapper::toView)
                       .toList();
    }

    public List<PartyRelationshipView> findMatching(Predicate<PartyRelationship> predicate) {
        return repository.findMatching(predicate).stream()
                         .map(PartyRelationshipViewMapper::toView)
                         .toList();
    }
}

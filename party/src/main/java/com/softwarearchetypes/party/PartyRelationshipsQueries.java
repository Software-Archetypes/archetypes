package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class PartyRelationshipsQueries {

    private final PartyRelationshipRepository repository;

    PartyRelationshipsQueries(PartyRelationshipRepository repository) {
        this.repository = repository;
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

    public List<PartyRelationship> findAllRelationsFrom(List<PartyId> partyIds, RelationshipName name) {
        return partyIds.stream().flatMap(it -> repository.findAllRelationsFrom(it, name).stream()).collect(Collectors.toList());
    }

    public List<PartyRelationship> findMatching(List<PartyId> partyIds, RelationshipName name) {
        return partyIds.stream().flatMap(it -> repository.findAllRelationsFrom(it, name).stream()).collect(Collectors.toList());
    }

    public List<PartyRelationship> findMatching(Predicate<PartyRelationship> predicate) {
        return repository.findMatching(predicate);
    }
}

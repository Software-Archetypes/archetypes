package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class InMemoryPartyRelationshipRepository implements PartyRelationshipRepository {

    private final ConcurrentHashMap<PartyRelationshipId, PartyRelationship> map = new ConcurrentHashMap<>(10);

    @Override
    public List<PartyRelationship> findAllRelationsFrom(PartyId partyId) {
        return map.values().parallelStream()
                  .filter(rel -> rel.from().partyId().equals(partyId))
                  .collect(Collectors.toList());
    }

    @Override
    public List<PartyRelationship> findAllRelationsFrom(PartyId partyId, RelationshipName name) {
        return map.values().parallelStream()
                  .filter(rel -> rel.name().equals(name) && rel.from().partyId().equals(partyId))
                  .collect(Collectors.toList());
    }

    @Override
    public Optional<PartyRelationship> findBy(PartyRelationshipId relationshipId) {
        return Optional.ofNullable(map.get(relationshipId));
    }

    @Override
    public void save(PartyRelationship partyRelationship) {
        map.put(partyRelationship.id(), partyRelationship);
    }

    @Override
    public Optional<PartyRelationshipId> delete(PartyRelationshipId relationshipId) {
        PartyRelationship result = map.remove(relationshipId);
        return Optional.ofNullable(result).map(PartyRelationship::id);
    }

    @Override
    public List<PartyRelationship> findMatching(Predicate<PartyRelationship> predicate) {
        return map.values().parallelStream()
                  .filter(predicate)
                  .collect(Collectors.toList());
    }
}

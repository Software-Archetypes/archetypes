package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

class InMemoryPartyRepository implements PartyRepository {

    private ConcurrentHashMap<PartyId, Party> map = new ConcurrentHashMap<>(10);

    @Override
    public Optional<Party> findBy(PartyId partyId) {
        return Optional.ofNullable(map.get(partyId));
    }

    @Override
    public Optional<Party> findBy(PartyId partyId, Class<? extends Party> partyType) {
        return Optional.ofNullable(map.get(partyId)).filter(it -> partyType.isAssignableFrom(it.getClass()));
    }

    @Override
    public void save(Party party) {
        map.put(party.id(), party);
    }

    @Override
    public void delete(PartyId partyId) {
        map.remove(partyId);
    }

    @Override
    public List<Party> findBy(RegisteredIdentifier registeredIdentifier) {
        return map.values().parallelStream()
                  .filter(party -> party.registeredIdentifiers().contains(registeredIdentifier))
                  .collect(Collectors.toList());
    }

    @Override
    public List<Party> findMatching(Predicate<Party> predicate) {
        return map.values().parallelStream()
                  .filter(predicate)
                  .collect(Collectors.toList());
    }
}

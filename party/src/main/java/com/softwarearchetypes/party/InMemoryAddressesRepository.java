package com.softwarearchetypes.party;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

class InMemoryAddressesRepository implements AddressesRepository {

    private ConcurrentHashMap<PartyId, Addresses> map = new ConcurrentHashMap<>(10);

    @Override
    public Optional<Addresses> findFor(PartyId partyId) {
        return Optional.ofNullable(map.get(partyId));
    }

    @Override
    public List<Address> findMatching(PartyId partyId, Predicate<Address> predicate) {
        return findFor(partyId).map(Addresses::asSet).orElse(new HashSet<>())
                               .stream()
                               .filter(predicate)
                               .collect(toList());
    }

    @Override
    public void save(Addresses addresses) {
        map.put(addresses.partyId(), addresses);
    }
}

package com.softwarearchetypes.party;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

class InMemoryAddressesRepository implements AddressesRepository {

    private ConcurrentHashMap<PartyId, Addresses> map = new ConcurrentHashMap<>(10);

    @Override
    public Optional<Addresses> findFor(PartyId partyId) {
        return Optional.ofNullable(map.get(partyId));
    }

    @Override
    public void save(Addresses addresses) {
        map.put(addresses.partyId(), addresses);
    }
}

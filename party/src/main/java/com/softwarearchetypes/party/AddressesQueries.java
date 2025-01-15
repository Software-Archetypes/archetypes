package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class AddressesQueries {

    private final AddressesRepository repository;

    AddressesQueries(AddressesRepository repository) {
        this.repository = repository;
    }

    public Optional<Addresses> findFor(PartyId partyId) {
        return repository.findFor(partyId);
    }

    public List<Address> findMatching(PartyId partyId, Predicate<Address> predicate) {
        return repository.findMatching(partyId, predicate);
    }

}

package com.softwarearchetypes.party;

import java.util.List;
import java.util.function.Predicate;

public class AddressesQueries {

    private final AddressesRepository repository;

    AddressesQueries(AddressesRepository repository) {
        this.repository = repository;
    }

    public List<AddressView> findAllFor(PartyId partyId) {
        return repository.findFor(partyId)
                         .map(Addresses::asSet)
                         .orElse(java.util.Set.of())
                         .stream()
                         .map(AddressViewMapper::toView)
                         .toList();
    }

    /**
     * Sample query using predicate on domain objects.
     * NOTE: In production, replace with proper Criteria pattern.
     */
    List<AddressView> findMatching(PartyId partyId, Predicate<Address> predicate) {
        return repository.findFor(partyId)
                         .map(Addresses::asSet)
                         .orElse(java.util.Set.of())
                         .stream()
                         .filter(predicate)
                         .map(AddressViewMapper::toView)
                         .toList();
    }

    // In the future, we can add criteria-based queries like:
    // public List<AddressView> findMatching(PartyId partyId, AddressCriteria criteria)

}

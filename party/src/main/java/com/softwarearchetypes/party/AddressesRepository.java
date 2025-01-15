package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

interface AddressesRepository {

    Optional<Addresses> findFor(PartyId partyId);

    List<Address> findMatching(PartyId partyId, Predicate<Address> predicate);

    void save(Addresses addresses);
}

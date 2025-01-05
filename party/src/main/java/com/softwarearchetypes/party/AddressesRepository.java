package com.softwarearchetypes.party;

import java.util.Optional;

interface AddressesRepository {

    Optional<Addresses> findFor(PartyId partyId);

    void save(Addresses addresses);
}

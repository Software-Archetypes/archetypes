package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;

public interface PartyRepository {

    Optional<Party> findBy(PartyId partyId);

    Optional<Party> findBy(PartyId partyId, Class<? extends Party> partyType);

    void save(Party party);

    void delete(PartyId partyId);

    List<Party> findBy(RegisteredIdentifier registeredIdentifier);
}

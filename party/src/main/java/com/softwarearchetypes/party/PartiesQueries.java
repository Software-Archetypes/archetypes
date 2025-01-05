package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;

import static java.lang.String.format;

class PartiesQueries {

    private final PartyRepository partyRepository;

    PartiesQueries(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    Optional<Party> findBy(PartyId partyId) {
        return partyRepository.findBy(partyId);
    }

    Optional<Party> findOneBy(RegisteredIdentifier registeredIdentifier) {
        List<Party> partiesMatching = partyRepository.findBy(registeredIdentifier);
        if (partiesMatching.size() > 1) {
            throw new IllegalStateException(format("There are more than one parties with the same registered identifier of %s", registeredIdentifier));
        }
        return partiesMatching.stream().findFirst();
    }

    //find by Id
    //find by identity
    //find by role
    //etc
}

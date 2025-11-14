package com.softwarearchetypes.party;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.String.format;

public class PartiesQueries {

    private final PartyRepository partyRepository;

    PartiesQueries(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    public Optional<PartyView> findBy(PartyId partyId) {
        return partyRepository.findBy(partyId)
                              .map(PartyViewMapper::toView);
    }

    public Optional<PartyView> findOneBy(RegisteredIdentifier registeredIdentifier) {
        List<Party> partiesMatching = partyRepository.findBy(registeredIdentifier);
        if (partiesMatching.size() > 1) {
            throw new IllegalStateException(format("There are more than one parties with the same registered identifier of %s", registeredIdentifier));
        }
        return partiesMatching.stream()
                              .findFirst()
                              .map(PartyViewMapper::toView);
    }

    public List<PartyView> findMatching(Predicate<Party> predicate) {
        return partyRepository.findMatching(predicate).stream()
                              .map(PartyViewMapper::toView)
                              .toList();
    }

    //find by Id
    //find by identity
    //find by role
    //etc
}

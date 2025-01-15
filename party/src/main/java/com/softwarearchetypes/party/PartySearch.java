package com.softwarearchetypes.party;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

public class PartySearch {

    private final PartiesQueries partiesQueries;
    private final PartyRelationshipsQueries partyRelationshipsQueries;
    private final AddressesQueries addressesQueries;

    PartySearch(PartiesQueries partiesQueries, PartyRelationshipsQueries partyRelationshipsQueries, AddressesQueries addressesQueries) {
        this.partiesQueries = partiesQueries;
        this.partyRelationshipsQueries = partyRelationshipsQueries;
        this.addressesQueries = addressesQueries;
    }

    //sample query - here is the place where we could apply graph database queries
    List<Party> findMatching(Search search) {
        List<Party> result = new LinkedList<>();
        if (search.partyPredicate != null) {
            result = partiesQueries.findMatching(search.partyPredicate);
        }
        if (search.partyRelationshipPredicate != null) {
            if (!result.isEmpty()) {
                List<PartyId> foundIds = result.stream().map(Party::id).toList();
                List<PartyId> toPartyIds = partyRelationshipsQueries
                        .findMatching(it -> foundIds.contains(it.from().partyId()) && search.partyRelationshipPredicate.test(it))
                        .stream()
                        .map(rel -> rel.to().partyId())
                        .toList();
                result = partiesQueries.findMatching(it -> toPartyIds.contains(it.id()));
            }
        }
        if (search.addressPredicate != null) {
            if (!result.isEmpty()) {
                result = result.stream().filter(party -> !addressesQueries.findMatching(party.id(), search.addressPredicate).isEmpty()).collect(toList());
            }
        }
        return result;
    }

    //we could add builder and spec pattern
    record Search(Predicate<Party> partyPredicate,
                  Predicate<PartyRelationship> partyRelationshipPredicate,
                  Predicate<Address> addressPredicate) {

    }
}

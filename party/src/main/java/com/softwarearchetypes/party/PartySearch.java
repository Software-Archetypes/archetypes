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

    /**
     * Sample query implementation using predicates on domain objects.
     * NOTE: This is an example implementation. In production, this should be replaced with:
     * - Search Criteria objects instead of Predicates
     * - Direct database queries (e.g., SQL joins, graph database queries)
     * - Avoiding loading domain objects just for filtering
     *
     * This is the place where we could apply graph database queries or advanced search engines.
     */
    List<PartyView> findMatching(Search search) {
        List<PartyView> result = new LinkedList<>();
        if (search.partyPredicate != null) {
            result = partiesQueries.findMatching(search.partyPredicate);
        }
        if (search.partyRelationshipPredicate != null) {
            if (!result.isEmpty()) {
                List<PartyId> foundIds = result.stream().map(PartyView::partyId).toList();
                List<PartyId> toPartyIds = partyRelationshipsQueries
                        .findMatching(it -> foundIds.contains(it.from().partyId()) && search.partyRelationshipPredicate.test(it))
                        .stream()
                        .map(PartyRelationshipView::toPartyId)
                        .toList();
                result = partiesQueries.findMatching(it -> toPartyIds.contains(it.id()));
            }
        }
        if (search.addressPredicate != null) {
            if (!result.isEmpty()) {
                result = result.stream()
                        .filter(partyView -> !addressesQueries.findMatching(partyView.partyId(), search.addressPredicate).isEmpty())
                        .collect(toList());
            }
        }
        return result;
    }

    /**
     * Search criteria using predicates on domain objects.
     * NOTE: In production, replace with proper Criteria/Specification pattern.
     */
    record Search(Predicate<Party> partyPredicate,
                  Predicate<PartyRelationship> partyRelationshipPredicate,
                  Predicate<Address> addressPredicate) {

    }
}
